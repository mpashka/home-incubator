// @tag:auth
package dev.homeincubator.lngedu.auth;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure unit test for the RFC 8707 audience check ({@code @tag:auth}): a token is accepted only when
 * its {@code aud} contains the MCP resource identifier.
 */
class AudienceValidatorTest {

    private static final String RESOURCE = "http://localhost:8097";
    private final AudienceValidator validator = new AudienceValidator(RESOURCE);

    @Test
    void acceptsTokenWhoseAudienceContainsTheResource() {
        Jwt jwt = jwtWithAudience(List.of("http://other", RESOURCE));
        OAuth2TokenValidatorResult result = validator.validate(jwt);
        assertThat(result.hasErrors()).isFalse();
    }

    @Test
    void rejectsTokenWithWrongAudience() {
        Jwt jwt = jwtWithAudience(List.of("http://someone-else"));
        OAuth2TokenValidatorResult result = validator.validate(jwt);
        assertThat(result.hasErrors()).isTrue();
    }

    @Test
    void rejectsTokenWithNoAudience() {
        Jwt jwt = Jwt.withTokenValue("t")
                .header("alg", "RS256")
                .subject("acc")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
        OAuth2TokenValidatorResult result = validator.validate(jwt);
        assertThat(result.hasErrors()).isTrue();
    }

    private static Jwt jwtWithAudience(List<String> audience) {
        return Jwt.withTokenValue("t")
                .header("alg", "RS256")
                .subject("acc")
                .audience(audience)
                .claim("account_id", "acc")
                .claims(claims -> claims.putIfAbsent("extra", Map.of()))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
    }
}
