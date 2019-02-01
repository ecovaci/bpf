package org.kpax.bpf.exception;

/**
 * Thrown when command line execution fails.
 */
public class CommandExecutionException extends Exception {

	private static final long serialVersionUID = -1082218489871862086L;

	public CommandExecutionException(String message) {
		super(message);
	}

	public CommandExecutionException(String message, Throwable cause) {
		super(message, cause);
	}

	public CommandExecutionException(Throwable cause) {
		super(cause);
	}

	public CommandExecutionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
