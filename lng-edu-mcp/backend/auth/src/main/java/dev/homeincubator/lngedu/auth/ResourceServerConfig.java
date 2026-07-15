// @tag:auth
package dev.homeincubator.lngedu.auth;

import java.util.HashSet;
import java.util.Set;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * OAuth2 Resource Server for the protected MCP + REST surface (Phase I, ADR 0002, {@code @tag:auth}).
 *
 * <p>This {@code @Order(0)} filter chain has a {@code securityMatcher} for the protected surface
 * ({@code /api/**}, {@code /sse}, {@code /mcp/**}) and requires a valid JWT bearer token. It runs
 * before the Authorization Server chain ({@code @Order(1)}) and the login chain ({@code @Order(2)});
 * requests outside its matcher (the AS endpoints, the login UI, the PRM document, actuator health,
 * OpenAPI/Swagger) fall through to those chains and stay open.
 *
 * <p>Tokens are validated against BOTH the issuer (our Authorization Server, {@code AUTH_ISSUER_URI})
 * and the audience (must contain {@code MCP_RESOURCE_URI}). Signatures are verified with the same
 * in-process {@link JWKSource} the Authorization Server signs with, so validation needs no network
 * round-trip to the issuer's JWKS at startup. On a 401 the {@link ResourceMetadataAuthenticationEntryPoint}
 * advertises the RFC 9728 Protected Resource Metadata location via {@code WWW-Authenticate}.
 */
@Configuration
public class ResourceServerConfig {

    @Bean
    @Order(0)
    public SecurityFilterChain resourceServerSecurityFilterChain(
            HttpSecurity http,
            JWKSource<SecurityContext> jwkSource,
            @Value("${AUTH_ISSUER_URI:http://localhost:8080}") String issuer,
            @Value("${MCP_RESOURCE_URI:http://localhost:8080}") String mcpResource) throws Exception {

        JwtDecoder decoder = resourceServerJwtDecoder(jwkSource, issuer, mcpResource);
        ResourceMetadataAuthenticationEntryPoint entryPoint =
                new ResourceMetadataAuthenticationEntryPoint(mcpResource);

        http
                .securityMatcher("/api/**", "/sse", "/mcp/**")
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                // Bearer-token resource server: stateless, no CSRF (the MCP /mcp/message POST carries
                // no browser session; it authenticates with the JWT on every call).
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationEntryPoint(entryPoint)
                        .jwt(jwt -> jwt.decoder(decoder)));

        return http.build();
    }

    /**
     * Resource-server JWT decoder: verifies RSA signatures against the in-process {@link JWKSource}
     * and enforces issuer + audience (plus the default expiry checks). Built inline (not as a
     * {@code JwtDecoder} bean) to avoid colliding with the Authorization Server's own decoder bean.
     */
    private static JwtDecoder resourceServerJwtDecoder(
            JWKSource<SecurityContext> jwkSource, String issuer, String resource) {
        Set<JWSAlgorithm> algorithms = new HashSet<>(JWSAlgorithm.Family.RSA);
        ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
        JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(algorithms, jwkSource);
        jwtProcessor.setJWSKeySelector(keySelector);
        // Claim validation (issuer/audience/expiry) is done by Spring's OAuth2TokenValidator below,
        // so disable Nimbus's own default claims verifier.
        jwtProcessor.setJWTClaimsSetVerifier((claims, context) -> { });

        NimbusJwtDecoder decoder = new NimbusJwtDecoder(jwtProcessor);
        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(issuer),
                new AudienceValidator(resource));
        decoder.setJwtValidator(validator);
        return decoder;
    }
}
