package org.memnon.exception;

/**
 * 类加载异常
 */
public class ActionNotFoundException extends CommonException {
    public ActionNotFoundException(String message) {
        super(message);
    }

    public ActionNotFoundException(Throwable cause) {
        super(cause);
    }
}
