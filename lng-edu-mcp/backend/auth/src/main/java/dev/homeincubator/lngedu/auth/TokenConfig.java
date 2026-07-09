// @tag:auth
package dev.homeincubator.lngedu.auth;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.UUID;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import dev.homeincubator.lngedu.account.AccountService;
import dev.homeincubator.lngedu.account.AppAccount;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

/**
 * JWT signing key + token shaping (Phase H, {@code @tag:auth}).
 *
 * <p>The signing key is an <strong>ephemeral RSA key generated at startup</strong>: it is fine for
 * dev but means issued tokens (and the published JWKS) do NOT survive a restart. A durable key
 * (keystore/env) is a later concern.
 *
 * <p>The token customizer derives the {@code app_account} from the federated Google principal
 * (provider {@code google}, {@code sub}, email) via {@link AccountService#resolveByExternalIdentity}
 * (idempotent upsert), then puts that account id in the token {@code sub} and an explicit
 * {@code account_id} claim, and stamps the MCP resource {@code aud} on the access token.
 */
@Configuration
public class TokenConfig {

    /** RSA JWK source. Ephemeral: regenerated on every application start (dev only). */
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    /**
     * Stamp the resolved {@code app_account} id into the token and add the MCP {@code aud}.
     *
     * @param mcpResource env {@code MCP_RESOURCE_URI}, dev default {@code http://localhost:8080}
     */
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer(
            AccountService accountService,
            @Value("${MCP_RESOURCE_URI:http://localhost:8080}") String mcpResource) {
        return context -> {
            UUID accountId = resolveAccountId(accountService, context);
            if (accountId != null) {
                // app_account id as both the subject and an explicit claim (later phases read either).
                context.getClaims().subject(accountId.toString());
                context.getClaims().claim("account_id", accountId.toString());
            }
            if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                // RFC 8707 resource indicator / audience for the MCP resource server (Phase I validates it).
                context.getClaims().audience(List.of(mcpResource));
            }
        };
    }

    /**
     * Resolve the app_account id from the authenticated principal. On federated Google login the
     * principal is an {@link OidcUser} (or {@link OAuth2User}) carrying {@code sub} and email.
     */
    private static UUID resolveAccountId(AccountService accountService, JwtEncodingContext context) {
        Object principal = context.getPrincipal() != null ? context.getPrincipal().getPrincipal() : null;
        String subject = null;
        String email = null;
        if (principal instanceof OidcUser oidcUser) {
            subject = oidcUser.getSubject();
            email = oidcUser.getEmail();
        } else if (principal instanceof OAuth2User oAuth2User) {
            subject = (String) oAuth2User.getAttributes().get("sub");
            email = (String) oAuth2User.getAttributes().get("email");
        }
        if (subject == null) {
            return null;
        }
        AppAccount account = accountService.resolveByExternalIdentity("google", subject, email);
        return account.getId();
    }

    private static KeyPair generateRsaKey() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to generate RSA key for JWT signing", ex);
        }
    }
}
