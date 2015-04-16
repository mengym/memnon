package org.memnon.exception;

/**
 * 初始化异常
 */
public class InitException extends RuntimeException {

    public InitException() {
        super();
    }

    public InitException(String message) {
        super(message);
    }

    public InitException(String message, Throwable cause) {
        super(message + ": " + cause.getClass() + ":" + cause.getMessage(), cause);
        setStackTrace(cause.getStackTrace());
    }

    public InitException(Throwable cause) {
        super(cause);
        setStackTrace(cause.getStackTrace());
    }
}
