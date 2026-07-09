package dev.homeincubator.lngedu.common;

/**
 * Domain exception for a missing entity. A later phase maps it to a Problem Details 404.
 * Kept transport-agnostic on purpose (no REST/MCP annotations here).
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException of(String what, Object id) {
        return new NotFoundException(what + " not found: " + id);
    }
}
