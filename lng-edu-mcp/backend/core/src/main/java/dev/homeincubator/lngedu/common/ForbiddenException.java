// @tag:auth @tag:account-linking
package dev.homeincubator.lngedu.common;

/**
 * Domain exception for an authenticated caller that may not access the referenced resource
 * (e.g. a learner profile it does not own). The REST adapter maps it to a Problem Details 403.
 * Kept transport-agnostic on purpose (no REST/MCP annotations here).
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
