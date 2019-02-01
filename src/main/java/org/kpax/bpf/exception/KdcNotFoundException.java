package org.kpax.bpf.exception;

/**
 * Thrown when no KDC server is found.
 */
public class KdcNotFoundException extends Exception {

    private static final long serialVersionUID = -115057186623212419L;

    public KdcNotFoundException() {
    }

    public KdcNotFoundException(String message) {
        super(message);
    }

    public KdcNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public KdcNotFoundException(Throwable cause) {
        super(cause);
    }

    public KdcNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
