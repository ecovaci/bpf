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

package org.kpax.bpf.util;

import java.net.Socket;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

/**
 * @author Eugen Covaci
 */
public class HttpUtils {

	public static final String STATUS_LINE_CONNECTION_ESTABLISHED = "200 Connection established";

	public static final String CONNECTION_ESTABLISHED = "Connection established";

	public static final String HTTP_CONNECT = "CONNECT";

	public static final String PROXY_CONNECTION = "Proxy-Connection";

	public static final int MAX_PORT_VALUE = 65535;

	public static URI parseConnectUri(String uri) throws NumberFormatException, URISyntaxException {
		String[] split = uri.split(":");
		return new URI(null, null, split[0], Integer.parseInt(split[1]), null, null, null);
	}

	public static URI parseUri(String url) throws URISyntaxException {
		int index = url.indexOf("?");
		if (index > -1 && index < url.length()) {
			URIBuilder uriBuilder = new URIBuilder(url.substring(0, index));
			List<NameValuePair> nvps = URLEncodedUtils.parse(url.substring(index + 1), Charset.forName("UTF-8"));
			uriBuilder.addParameters(nvps);
			return uriBuilder.build();
		}
		return new URIBuilder(url).build();
	}

	public static String connectionEstablished(ProtocolVersion version) {
		return version.toString() + StringUtils.SPACE + STATUS_LINE_CONNECTION_ESTABLISHED;
	}

	public static String stripChunked(String value) {
		return Arrays.stream(value.split(",")).filter((item) -> !HTTP.CHUNK_CODING.equalsIgnoreCase(item)).collect(Collectors.joining(","));
	}

	public static String getFirstHeaderValue(HttpRequest request, String name) {
		Header firstHeader = request.getFirstHeader(name);
		return firstHeader != null ? firstHeader.getValue() : null;
	}

	public static long getContentLength(HttpRequest request) {
		String contentLength = getFirstHeaderValue(request, HttpHeaders.CONTENT_LENGTH);
		return contentLength != null ? Long.parseLong(contentLength) : -1;
	}

	public static Header createHttpHeader(String name, String value) {
		return new BasicHeader(name, value);
	}

	public static String createStrHttpHeader(String name, String value) {
		return createHttpHeader(name, value).toString();
	}

	public static Socket tuneSocket(Socket socket, int bufferSize) throws SocketException {
		socket.setTcpNoDelay(true);
		socket.setReceiveBufferSize(bufferSize);
		socket.setSendBufferSize(bufferSize);
		return socket;
	}

}
