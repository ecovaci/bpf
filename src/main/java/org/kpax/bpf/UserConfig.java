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

package org.kpax.bpf;

import javax.annotation.PostConstruct;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @author Eugen Covaci
 */
@Component
@PropertySource(value = "file:${user.dir}/config/user.properties", name = "userProperties")
public class UserConfig {

	@Autowired
	private FileBasedConfigurationBuilder<PropertiesConfiguration> propertiesBuilder;

	@Value("${proxy.username}")
	private String username;

	private String password;

	@Value("${proxy.domain}")
	private String domain;

	@Value("${local.port}")
	private int localPort;

	@Value("${proxy.host}")
	private String proxyHost;

	@Value("${proxy.port ? : 0}")
	private int proxyPort;

	@PostConstruct
	public void init() {
		if (StringUtils.isEmpty(username)) {
			username = System.getProperty("user.name");
		}
		if (StringUtils.isEmpty(domain)) {
			boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
			if (isWindows) {
				domain = System.getenv("USERDOMAIN");
			}
		}

	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getDomain() {
		return domain;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getLocalPort() {
		return localPort;
	}

	public String getProxyHost() {
		return proxyHost;
	}

	public int getProxyPort() {
		return proxyPort;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public void setLocalPort(int localPort) {
		this.localPort = localPort;
	}

	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}

	public void save() throws ConfigurationException {
		Configuration config = propertiesBuilder.getConfiguration();
		config.setProperty("proxy.domain", this.domain);
		config.setProperty("local.port", this.localPort);
		config.setProperty("proxy.host", this.proxyHost);
		config.setProperty("proxy.port", this.proxyPort);
		config.setProperty("proxy.username", this.username);
		propertiesBuilder.save();
	}

	@Override
	public String toString() {
		return "UserConfig [username=" + username + ", domain=" + domain + ", localPort=" + localPort
				+ ", proxyHost=" + proxyHost + ", proxyPort=" + proxyPort + "]";
	}

}
