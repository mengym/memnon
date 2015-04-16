package org.memnon.exception;

/**
 * Created by Administrator on 2015/4/9.
 */
public class ControllerException extends CommonException {
    public ControllerException() {
        super();
    }

    public ControllerException(String message) {
        super(message);
    }

    public ControllerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ControllerException(Throwable cause) {
        super(cause);
    }
}
