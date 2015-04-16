package org.memnon.exception;

/**
 * 类加载异常
 */
public class ClassLoadException extends Exception {
    public ClassLoadException(String message) {
        super(message);
    }

    public ClassLoadException(Throwable cause) {
        super(cause);
    }
}
