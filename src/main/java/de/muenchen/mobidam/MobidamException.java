package de.muenchen.mobidam;

public class MobidamException extends Exception {

    public MobidamException(String message) {
        super(message);
    }

    public MobidamException(String message, Throwable cause) {
        super(message, cause);
    }
}
