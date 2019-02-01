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

package org.kpax.bpf.proxy;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.Validate;

/**
 * A helper class that wraps an {@link AsynchronousSocketChannel}.
 * 
 * @author Eugen Covaci
 */
class AsynchronousSocketChannelWrapper implements Closeable {

	private final AsynchronousSocketChannel socketChannel;

	private final InputStream inputStream;

	private final OutputStream outputStream;

	public AsynchronousSocketChannelWrapper(AsynchronousSocketChannel socketChannel) {
		Validate.notNull(socketChannel, "socketChannel cannot be null");
		this.socketChannel = socketChannel;
		inputStream = new SocketChannelInputStream();
		outputStream = new SocketChannelOutputStream();
	}

	public AsynchronousSocketChannel getSocketChannel() {
		return socketChannel;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public OutputStream getOutputStream() {
		return outputStream;
	}

	@Override
	public void close() throws IOException {
		socketChannel.close();
	}

	private class SocketChannelInputStream extends InputStream {

		@Override
		public int read() {
			throw new NotImplementedException("Do not use it");
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			ByteBuffer buffer = ByteBuffer.wrap(b, off, len);
			try {
				return socketChannel.read(buffer).get();
			} catch (ExecutionException e) {
				throw new IOException(e.getCause());
			} catch (Exception e) {
				throw new IOException(e);
			}

		}
	}

	private class SocketChannelOutputStream extends OutputStream {

		@Override
		public void write(int b) {
			throw new NotImplementedException("Do not use it");
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			ByteBuffer buffer = ByteBuffer.wrap(b, off, len);
			try {
				socketChannel.write(buffer).get();
			} catch (ExecutionException e) {
				throw new IOException(e.getCause());
			} catch (Exception e) {
				throw new IOException(e);
			}
		}
	}

}
