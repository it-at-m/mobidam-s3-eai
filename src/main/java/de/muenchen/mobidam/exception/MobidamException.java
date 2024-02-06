package de.muenchen.mobidam.exception;

public class MobidamException extends Exception {

    public MobidamException(String message) {
        super(message);
    }

    public MobidamException(String message, Throwable cause) {
        super(message, cause);
    }
}
