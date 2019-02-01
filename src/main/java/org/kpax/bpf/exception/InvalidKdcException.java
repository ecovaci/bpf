package org.kpax.bpf.exception;

/**
 * Thrown when at least one KDC server is found but it is not valid.
 */
public class InvalidKdcException extends Exception {

	private static final long serialVersionUID = -2992185009473469356L;

	public InvalidKdcException() {
	}

	public InvalidKdcException(String message) {
		super(message);
	}

	public InvalidKdcException(Throwable cause) {
		super(cause);
	}

	public InvalidKdcException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidKdcException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
