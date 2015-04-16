package org.memnon.exception;

/**
 * Created by Administrator on 2015/4/9.
 */
public class CommonException extends RuntimeException {
    public CommonException() {
        super();
    }

    public CommonException(String message) {
        super(message);
    }

    public CommonException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommonException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getMessage() {
        String message = super.getMessage();
        if (getCause() != null) {
            message += "; " + getCause().getMessage();
        }
        return message;
    }
}
