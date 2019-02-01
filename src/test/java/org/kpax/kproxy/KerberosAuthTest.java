/*
 * Copyright (c) 2018 Eugen Covaci.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and limitations under the License.
 */

package org.kpax.kproxy;

import java.io.File;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.kerby.kerberos.kerb.KrbException;
import org.apache.kerby.kerberos.kerb.server.SimpleKdcServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kpax.bpf.Application;
import org.kpax.bpf.UserConfig;
import org.kpax.bpf.auth.AuthenticationManager;
import org.kpax.bpf.proxy.LocalProxyServer;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class })
@TestPropertySource(locations = { "classpath:kproxy_test.properties" })
public class KerberosAuthTest {

	@Autowired
	private UserConfig userConfig;

	@Autowired
	private LocalProxyServer localServer;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private AbstractApplicationContext applicationContext;

	@Test
	public void testAuthentication() throws Exception {
		System.setProperty("sun.security.krb5.debug", "true");
		SimpleKdcServer simpleKdcServer = null;
		try {
			simpleKdcServer = new SimpleKdcServer();
			simpleKdcServer.enableDebug();
			simpleKdcServer.setKdcTcpPort(54284);
			simpleKdcServer.setAllowTcp(true);
			simpleKdcServer.setAllowUdp(false);
			simpleKdcServer.setKdcPort(54284);
			simpleKdcServer.setWorkDir(new File("src/test/resources"));
			simpleKdcServer.init();
			System.out.println("Start KDC server");
			simpleKdcServer.start();
			simpleKdcServer.createPrincipal("Quasimodo@EXAMPLE.COM", "1234");
			System.out.println("KDC server started and listening on port: " + simpleKdcServer.getKdcPort());
			System.setProperty("java.security.auth.login.config", new File("config/jaas.conf").getAbsolutePath());
			System.setProperty("java.security.krb5.conf", new File("src/test/resources/krb5.conf").getAbsolutePath());
			
			userConfig.setDomain("example.com");
			userConfig.setProxyHost("localhost");
			userConfig.setProxyPort(54284);
			userConfig.setUsername("Quasimodo");
			userConfig.setPassword("1234");
			
			System.out.println("Try to authenticate");
			authenticationManager.authenticate();
			System.out.println("Authenticated OK");
		} finally {
			if (simpleKdcServer != null) {
				try {
					System.out.println("Stop KDC server");
					simpleKdcServer.stop();
				} catch (KrbException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Test
	public void testRequests() throws Exception {
		HttpProxyServer server;
		try {
			System.out.println("Start http server");

			server = DefaultHttpProxyServer.bootstrap().withPort(54284).start();

			System.out.println("Start proxy facade with userConfig: " + userConfig);
			localServer.start();
			System.out.println("Proxy facade started");

			try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
				HttpHost proxy = new HttpHost("localhost", 3129, "http");
				RequestConfig config = RequestConfig.custom().setProxy(proxy).build();
				executeRequest(httpClient, config, proxy, "http", "example.com", 80);
				executeRequest(httpClient, config, proxy, "https", "example.com", 443);
			} finally {
				server.stop();
			}
		} finally {
			applicationContext.close();
		}
	}

	private void executeRequest(CloseableHttpClient httpClient, RequestConfig config, HttpHost proxy, String protocol,
			String host, int port) throws Exception {
		HttpGet httpRequest = new HttpGet("/");
		httpRequest.setConfig(config);
		HttpHost httpTarget = new HttpHost(host, port, protocol);
		System.out.println("Executing request " + httpRequest.getRequestLine() + " to " + httpTarget + " via " + proxy);
		try (CloseableHttpResponse httpResponse = httpClient.execute(httpTarget, httpRequest)) {
			System.out.println("----------------------------------------");
			System.out.println(httpResponse.getStatusLine());
			EntityUtils.consume(httpResponse.getEntity());
		}
	}

}
