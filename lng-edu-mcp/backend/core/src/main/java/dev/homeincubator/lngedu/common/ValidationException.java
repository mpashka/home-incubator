package dev.homeincubator.lngedu.common;

/**
 * Domain exception for invalid input. A later phase maps it to a Problem Details 400.
 * Kept transport-agnostic on purpose (no REST/MCP annotations here).
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}
