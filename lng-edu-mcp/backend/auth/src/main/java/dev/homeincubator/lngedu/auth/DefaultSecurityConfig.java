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
 * <p>IMPORTANT for Phase H: every existing application endpoint is PERMITTED here so current
 * behaviour and tests keep working ({@code /api}, {@code /sse}, {@code /mcp}, actuator, OpenAPI /
 * Swagger, and the AS metadata all stay open). Only the AS authorization flow (the {@code @Order(1)}
 * chain) requires login. Phase I introduces the resource-server chain that actually protects
 * {@code /api} and {@code /sse}.
 */
@Configuration
public class DefaultSecurityConfig {

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // Phase H: keep the whole app surface open; do not protect /api or /sse yet.
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                // Federated login into Google for the AS authentication UI.
                .oauth2Login(Customizer.withDefaults());
        return http.build();
    }
}
