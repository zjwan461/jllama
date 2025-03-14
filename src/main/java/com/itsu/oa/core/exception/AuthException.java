package com.itsu.oa.core.exception;

public class AuthException extends JException {

    public AuthException(String message) {
        super(message);
    }

    public AuthException(Throwable cause) {
        super(cause);
    }

    public AuthException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthException() {
    }
}
