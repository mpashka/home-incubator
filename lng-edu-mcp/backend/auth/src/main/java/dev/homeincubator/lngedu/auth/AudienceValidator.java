// @tag:auth
package dev.homeincubator.lngedu.auth;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Audience validator (RFC 8707, {@code @tag:auth}). A JWT access token is accepted by the MCP
 * resource server only if its {@code aud} claim contains the MCP resource identifier
 * ({@code MCP_RESOURCE_URI}). Runs alongside the default issuer/expiry validators.
 */
public class AudienceValidator implements OAuth2TokenValidator<Jwt> {

    private final String requiredAudience;

    public AudienceValidator(String requiredAudience) {
        this.requiredAudience = requiredAudience;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        if (jwt.getAudience() != null && jwt.getAudience().contains(requiredAudience)) {
            return OAuth2TokenValidatorResult.success();
        }
        return OAuth2TokenValidatorResult.failure(new OAuth2Error(
                OAuth2ErrorCodes.INVALID_TOKEN,
                "The required audience '" + requiredAudience + "' is missing",
                null));
    }
}
