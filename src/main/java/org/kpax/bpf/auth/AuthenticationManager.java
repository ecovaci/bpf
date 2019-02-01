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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivilegedAction;

import javax.annotation.PostConstruct;

import org.kpax.bpf.exception.CommandExecutionException;
import org.kpax.bpf.exception.InvalidKdcException;
import org.kpax.bpf.exception.KdcNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Handles the Kerberos authentication.
 *
 * @author Eugen Covaci
 */
@Component
public class AuthenticationManager implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationManager.class);

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * The current Kerberos authenticator.
     */
    private volatile KerberosAuthenticator currentAuthenticator;

    @PostConstruct
    public void init() {

        // Set jaas.conf file
        String jaasConfigPath = new File("config/jaas.conf").getAbsolutePath();
        logger.info("Set java.security.auth.login.config property={}", jaasConfigPath);
        System.setProperty("java.security.auth.login.config", jaasConfigPath);

        // Check for krb5.conf
        File krbConfigFile = new File("config/krb5.conf");
        if (krbConfigFile.exists()) {
            String krbConfigPath = krbConfigFile.getAbsolutePath();
            logger.info("Set java.security.krb5.conf={}", krbConfigPath);
            System.setProperty("java.security.krb5.conf", krbConfigPath);
        } else {
            logger.info("'krb5.conf' file is missing from 'config' directory!");
        }

    }

    /**
     * It creates a new logged in Kerberos authenticator if the current one is not
     * valid.
     *
     * @throws GeneralSecurityException
     * @throws InvalidKdcException
     */
    public KerberosAuthenticator authenticate()
            throws GeneralSecurityException, KdcNotFoundException, CommandExecutionException, InvalidKdcException {
        if (currentAuthenticator == null || !currentAuthenticator.isValid()) {
            synchronized (this) {
                if (currentAuthenticator == null || !currentAuthenticator.isValid()) {
                    KerberosAuthenticator authenticator = applicationContext.getBean(KerberosAuthenticator.class);
                    authenticator.login();
                    logger.info("New authenticator created");
                    currentAuthenticator = authenticator;
                }
            }
        }
        return currentAuthenticator;
    }

    /**
     * If necessary authenticate and execute a privileged action within Kerberos context.
     *
     * @param action The action to be executed.
     * @param <T>
     * @throws GeneralSecurityException  when authentication failed.
     * @throws KdcNotFoundException      when no KDC server is found.
     * @throws CommandExecutionException when getting KDC server list failed.
     * @throws InvalidKdcException       when at least one KDC server is found but it is not valid.
     */
    public <T> void executePrivileged(PrivilegedAction<T> action)
            throws GeneralSecurityException, KdcNotFoundException, CommandExecutionException, InvalidKdcException {
        authenticate().execute(action);
    }

    @Override
    public void close() throws IOException {
        if (currentAuthenticator != null) {
            currentAuthenticator.logout();
        }
    }

}
