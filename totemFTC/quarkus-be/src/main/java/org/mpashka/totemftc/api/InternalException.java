package org.mpashka.totemftc.api;

/**
 * This exception must not be thrown unless there is a bug.
 */
public class InternalException extends RuntimeException {

    public InternalException(String message) {
        super(message);
    }
}
