// @tag:auth
package dev.homeincubator.lngedu.auth;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

/**
 * Spring Authorization Server wiring (Phase H, ADR 0002, {@code @tag:auth}).
 *
 * <p>The {@code @Order(1)} filter chain owns only the OAuth2/OIDC Authorization Server endpoints
 * (via the configurer's endpoint matcher): authorization, token, JWKS, OIDC discovery and — enabled
 * here — the OIDC Dynamic Client Registration endpoint so ChatGPT can register dynamically. PKCE for
 * public clients is on by default and is not disabled. Unauthenticated browser requests to the AS
 * are redirected into the federated Google login ({@code /oauth2/authorization/google}).
 */
@Configuration
@EnableWebSecurity
public class AuthorizationServerConfig {

    /** AS endpoint filter chain: OIDC + Dynamic Client Registration enabled. */
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                OAuth2AuthorizationServerConfigurer.authorizationServer();

        http
                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                .with(authorizationServerConfigurer, (authorizationServer) -> authorizationServer
                        // Enable OIDC and, within it, the OIDC Dynamic Client Registration endpoint
                        // (adds `registration_endpoint` to the discovery metadata).
                        .oidc(oidc -> oidc.clientRegistrationEndpoint(Customizer.withDefaults())))
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                // Redirect unauthenticated browser requests to the AS into the Google login.
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/oauth2/authorization/google"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)));

        return http.build();
    }

    /**
     * Configurable issuer (env {@code AUTH_ISSUER_URI}, dev default {@code http://localhost:8080}).
     * RFC 9207 {@code iss} and the discovery metadata are derived from it.
     */
    @Bean
    public AuthorizationServerSettings authorizationServerSettings(
            @Value("${AUTH_ISSUER_URI:http://localhost:8080}") String issuer) {
        return AuthorizationServerSettings.builder()
                .issuer(issuer)
                .build();
    }

    /**
     * Registered clients. Seeded with one public (PKCE, {@link ClientAuthenticationMethod#NONE})
     * demo client so the in-memory repository is non-empty at boot and can be exercised manually;
     * ChatGPT and other clients register themselves at runtime through the DCR endpoint (this
     * in-memory store also persists those for the lifetime of the process).
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        RegisteredClient demoPublicClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("mcp-demo-client")
                // Public client → no secret, PKCE required.
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("http://127.0.0.1:8097/callback")
                .redirectUri("http://localhost:8097/callback")
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.EMAIL)
                .scope("mcp")
                .clientSettings(ClientSettings.builder()
                        .requireProofKey(true)
                        .requireAuthorizationConsent(true)
                        .build())
                .build();
        return new InMemoryRegisteredClientRepository(demoPublicClient);
    }
}
