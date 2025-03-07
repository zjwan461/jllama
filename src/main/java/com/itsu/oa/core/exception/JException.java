package com.itsu.oa.core.exception;

public class JException extends RuntimeException {

    public JException(String message) {
        super(message);
    }

    public JException(Throwable cause) {
        super(cause);
    }

    public JException(String message, Throwable cause) {
        super(message, cause);
    }

    public JException() {
    }
}
