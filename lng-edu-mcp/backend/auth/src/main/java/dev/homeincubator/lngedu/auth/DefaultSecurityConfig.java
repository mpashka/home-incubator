// @tag:auth
package dev.homeincubator.lngedu.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Default (non-AS) security chain (Phase H, {@code @tag:auth}).
 *
 * <p>{@code @Order(2)} so it runs after the Authorization Server chain. It provides the federated
 * Google login UI ({@code oauth2Login}) used to authenticate the resource owner during the AS
 * authorization flow.
 *
 * <p>As of Phase I the protected surface ({@code /api/**}, {@code /sse}, {@code /mcp/**}) is owned by
 * the resource-server chain ({@link ResourceServerConfig}, {@code @Order(0)}) and the AS endpoints by
 * the {@code @Order(1)} chain. This {@code @Order(2)} chain only catches what is left — the login UI,
 * the Protected Resource Metadata document, actuator health and OpenAPI/Swagger — and keeps it open
 * (permitAll), while providing the Google login UI for the AS authorization flow.
 */
@Configuration
public class DefaultSecurityConfig {

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // Only non-protected, non-AS endpoints reach this chain (PRM, actuator, OpenAPI, login);
                // keep them open. /api, /sse and /mcp are protected by the @Order(0) resource server.
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                // Federated login into Google for the AS authentication UI.
                .oauth2Login(Customizer.withDefaults());
        return http.build();
    }
}
