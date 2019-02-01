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

import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @author Eugen Covaci
 */
@Component
@PropertySource("file:${user.dir}/config/system.properties")
public class SystemConfig {
	
	private static final Logger logger = LoggerFactory.getLogger(SystemConfig.class);

	@Value("${max.connections.per.route}")
	private Integer maxConnectionsPerRoute;

	@Value("${max.connections}")
	private Integer maxConnections;

	@Value("${ticket.threshold}")
	private Integer ticketThreshold;

	@Value("${eviction.period}")
	private Integer evictionPeriod;

	@Value("${max.connection.idle}")
	private Integer maxConnectionIdle;

	@Value("${server.socket.buffer.size}")
	private Integer serverSocketBufferSize;

	@Value("${socket.buffer.size}")
	private Integer socketBufferSize;

	@Value("${repeats.on.failure}")
	private Integer repeatsOnFailure;

	@Value("${eviction.enabled}")
	private boolean evictionEnabled;

	private String releaseVersion;

	@PostConstruct
	public void init() {
		try {
			logger.info("Get application version from manifest file");
			releaseVersion = new Manifest(Application.class.getResourceAsStream("/META-INF/MANIFEST.MF"))
					.getMainAttributes()
					.get(Attributes.Name.IMPLEMENTATION_VERSION).toString();
		} catch (Exception e) {
			logger.warn("Error on getting application version", e);
		}
	}

	public Integer getMaxConnectionsPerRoute() {
		return maxConnectionsPerRoute;
	}

	public Integer getMaxConnections() {
		return maxConnections;
	}

	public int getTicketThreshold() {
		return ticketThreshold;
	}

	public Integer getEvictionPeriod() {
		return evictionPeriod;
	}

	public Integer getServerSocketBufferSize() {
		return serverSocketBufferSize;
	}

	public Integer getSocketBufferSize() {
		return socketBufferSize;
	}

	public Integer getRepeatsOnFailure() {
		return repeatsOnFailure;
	}

	public Integer getMaxConnectionIdle() {
		return maxConnectionIdle;
	}

	public boolean isEvictionEnabled() {
		return evictionEnabled;
	}

	public String getReleaseVersion() {
		return releaseVersion;
	}

}
