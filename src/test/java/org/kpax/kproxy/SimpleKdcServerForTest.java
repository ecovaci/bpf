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

import org.apache.kerby.kerberos.kdc.impl.NettyKdcServerImpl;
import org.apache.kerby.kerberos.kerb.server.SimpleKdcServer;


public class SimpleKdcServerForTest {
    public static void main(String[] args) {
        int port = 3120;
        try {
            SimpleKdcServer simpleKdcServer = new SimpleKdcServer();
            simpleKdcServer.enableDebug();
            simpleKdcServer.setKdcTcpPort(port);
            simpleKdcServer.setKdcUdpPort(port);
            simpleKdcServer.setAllowTcp(true);
            simpleKdcServer.setAllowUdp(true);
            simpleKdcServer.setKdcPort(port);
            simpleKdcServer.setWorkDir(new File("./src/test/resources"));
            simpleKdcServer.setKdcRealm("EXAMPLE.COM");
           simpleKdcServer.setInnerKdcImpl(new NettyKdcServerImpl(simpleKdcServer.getKdcSetting()));
            simpleKdcServer.init();
            simpleKdcServer.start();
            simpleKdcServer.createPrincipal("Quasimodo@EXAMPLE.COM", "1234");
            System.out.println("Port: " + simpleKdcServer.getKdcPort());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
