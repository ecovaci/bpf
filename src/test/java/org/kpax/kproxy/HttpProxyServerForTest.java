package org.kpax.kproxy;

import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

public class HttpProxyServerForTest {

	public static void main(String[] args) {
		int port = 3120;
		try {
			System.out.println("Start http server on port: " + port);
			DefaultHttpProxyServer.bootstrap()
					.withPort(port)
					.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
