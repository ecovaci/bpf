/*******************************************************************************
 * Copyright (c) 2018 Eugen Covaci.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *
 * Contributors:
 *     Eugen Covaci - initial design and implementation
 *******************************************************************************/

package org.kpax.bpf.auth;

import java.security.GeneralSecurityException;
import java.security.PrivilegedAction;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.kerberos.KerberosTicket;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.kpax.bpf.SystemConfig;
import org.kpax.bpf.UserConfig;
import org.kpax.bpf.exception.CommandExecutionException;
import org.kpax.bpf.exception.InvalidKdcException;
import org.kpax.bpf.exception.KdcNotFoundException;
import org.kpax.bpf.util.CommandExecutor;
import org.kpax.bpf.util.LocalDateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * This class is responsible for Kerberos authentication,
 * logout and action execution within Kerberos context.
 *
 * @author Eugen Covaci
 */
@Component
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class KerberosAuthenticator {

    private static final Logger logger = LoggerFactory.getLogger(KerberosAuthenticator.class);

    @Autowired
    private UserConfig userConfig;

    @Autowired
    private SystemConfig config;

    private LoginContext loginContext;

    private Date validityDate;

    /**
     * Performs Kerberos authentication.
     * @throws GeneralSecurityException  when authentication failed.
     * @throws KdcNotFoundException      when no KDC server is found.
     * @throws CommandExecutionException when getting KDC server list failed.
     * @throws InvalidKdcException       when at least one KDC server is found but it is not valid.
     */
    @SuppressWarnings("restriction")
    void login()
            throws GeneralSecurityException, KdcNotFoundException,
            CommandExecutionException, InvalidKdcException {

        // The list of KDC servers, always empty when krb5.conf is set
        Set<String> kdcs = new HashSet<>();

        if (System.getProperty("java.security.krb5.conf") == null
                && System.getProperty("java.security.krb5.kdc") == null) {
            kdcs = CommandExecutor.nslookupKdc(userConfig.getDomain());
            logger.info("kdc list: {}", kdcs);
            if (kdcs.isEmpty()) {
                throw new KdcNotFoundException();
            }
            System.setProperty("java.security.krb5.realm", userConfig.getDomain().toUpperCase());
        }

        // Create login context
        this.loginContext = new LoginContext("JAASClient", callbacks -> {
            for (Callback callback : callbacks) {
                if (callback instanceof NameCallback) {
                    ((NameCallback) callback).setName(userConfig.getUsername() + "@" + userConfig.getDomain().toUpperCase());
                }
                if (callback instanceof PasswordCallback) {
                    ((PasswordCallback) callback).setPassword(userConfig.getPassword().toCharArray());
                }
            }
        });

        Iterator<String> kdcItr = kdcs.iterator();
        do {
            String kdc = null;
            if (kdcItr.hasNext()) {
                kdc = kdcItr.next();
                System.setProperty("java.security.krb5.kdc", kdc);
                try {
                    // We need this because the system properties have changed
                    sun.security.krb5.Config.refresh();
                } catch (Exception e) {
                    throw new KdcNotFoundException("Cannot refresh Kerberos context", e);
                }
            }

            try {
                // Authenticate
                logger.info("Try to authenticate with:");
                logger.info("* java.security.krb5.conf [{}]", System.getProperty("java.security.krb5.conf"));
                logger.info("* java.security.krb5.kdc [{}]", System.getProperty("java.security.krb5.kdc"));
                logger.info("* java.security.krb5.realm [{}]", System.getProperty("java.security.krb5.realm"));

                this.loginContext.login();
                if (kdc != null) {
                    logger.info("Authentication succeeded for kdc = [" + kdc + "]");
                }

                break;
            } catch (LoginException e) {
                if (kdcItr.hasNext()) {
                    logger.warn("Authentication failed for kdc = [" + kdc + "], try another kdc", e);
                } else {
                    if (kdc != null) {
                        System.clearProperty("java.security.krb5.kdc");
                        throw new InvalidKdcException(e);
                    }
                    throw e;
                }
            }

        } while (kdcItr.hasNext());

        // Retrieve the Kerberos credentials
        Subject subject = this.loginContext.getSubject();

        // Get Kerberos ticket
        KerberosTicket kerberosTicket = null;
        for (Object obj : subject.getPrivateCredentials()) {
            if (obj instanceof KerberosTicket) {
                kerberosTicket = ((KerberosTicket) obj);
            }
        }

        // Calculate the validity date
        Date endDate = kerberosTicket.getEndTime();
        logger.info("Expire date {}", endDate);
        this.validityDate = LocalDateUtils.substract(endDate, Calendar.SECOND, config.getTicketThreshold());
        logger.info("Validity date {}", this.validityDate);
    }

    /**
     * Logout the current user.
     */
    void logout() {
        if (loginContext != null) {
            logger.info("Logout authenticator");
            try {
                loginContext.logout();
            } catch (Exception e) {
                logger.warn("Error on logout", e);
            }
            validityDate = null;
        } else {
            logger.warn("No login found, nothing to do");
        }
    }

    /**
     * Check for Kerberos ticket's validity.
     *
     * @return
     */
    boolean isValid() {
        return validityDate != null && validityDate.after(new Date());
    }

    /**
     * Execute action in the current JAAS security context.
     *
     * @param action The action to be executed.
     * @return The result of processing.
     */
    <T> void execute(PrivilegedAction<T> action) {
        if (loginContext != null) {
            Subject.doAs(loginContext.getSubject(), action);
        } else {
            throw new SecurityException("Kerberos authentication not found");
        }

    }

    @Override
    protected void finalize() throws Throwable {
        logout();
    }

}
