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

package org.kpax.bpf.exception;

/**
 * Thrown when there is something wrong with application's configuration.
 * @author Eugen Covaci
 */
public class InvalidConfigException extends RuntimeException {

	private static final long serialVersionUID = 7395574835552647875L;

	public InvalidConfigException(String message) {
		super(message);
	}

	public InvalidConfigException(Throwable cause) {
		super(cause);
	}

	public InvalidConfigException(String message, Throwable cause) {
		super(message, cause);
	}

}
