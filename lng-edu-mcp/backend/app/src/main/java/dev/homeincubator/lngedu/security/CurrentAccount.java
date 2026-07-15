// @tag:auth @tag:account-linking
package dev.homeincubator.lngedu.security;

import java.util.UUID;

import dev.homeincubator.lngedu.common.ForbiddenException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Reads the authenticated app_account from the validated JWT in the SecurityContext (Phase I,
 * {@code @tag:auth}). This is the single place transport adapters (REST controllers and MCP tools)
 * turn the {@code account_id} claim of the bearer token into the owning account id, so the account
 * is always taken from the token and never from a client-supplied parameter (closes AGENTS rule 5).
 *
 * <p>It reads only the token; the actual profile-ownership check lives in the transport-agnostic
 * {@code AccountService.assertOwnsLearner(accountId, learnerId)}.
 */
@Component
public class CurrentAccount {

    /** The app_account id carried by the current request's validated JWT ({@code account_id} claim). */
    public UUID accountId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwt) {
            String claim = jwt.getToken().getClaimAsString("account_id");
            if (claim != null && !claim.isBlank()) {
                try {
                    return UUID.fromString(claim);
                } catch (IllegalArgumentException ex) {
                    throw new ForbiddenException("Malformed account_id claim in access token");
                }
            }
        }
        throw new ForbiddenException("No authenticated account in the security context");
    }
}
