// @tag:auth
package dev.homeincubator.lngedu.auth;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import dev.homeincubator.lngedu.account.AccountService;

/**
 * DB-free security-config test (Phase H, {@code @tag:auth}). Boots ONLY the Authorization Server /
 * login security configuration (no JPA, no Flyway, no real Google) and asserts:
 * <ul>
 *   <li>the OIDC discovery metadata is served with the core endpoints and the DCR endpoint;</li>
 *   <li>an unauthenticated AS request redirects into the Google login;</li>
 *   <li>{@code /oauth2/authorization/google} redirects to accounts.google.com (works with the dummy
 *       client id);</li>
 *   <li>the JWKS endpoint publishes a key.</li>
 * </ul>
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                // Dummy Google client so the ClientRegistrationRepository is created without real creds.
                "spring.security.oauth2.client.registration.google.client-id=dummy-client-id",
                "spring.security.oauth2.client.registration.google.client-secret=dummy-client-secret",
                "spring.security.oauth2.client.registration.google.scope=openid,email,profile",
                "AUTH_ISSUER_URI=http://localhost:8080"
        })
@AutoConfigureMockMvc
class AuthorizationServerMetadataTest {

    @org.springframework.beans.factory.annotation.Autowired
    private MockMvc mockMvc;

    @Test
    void servesOidcDiscoveryMetadataWithRegistrationEndpoint() throws Exception {
        mockMvc.perform(get("/.well-known/openid-configuration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.issuer").value("http://localhost:8080"))
                .andExpect(jsonPath("$.authorization_endpoint").exists())
                .andExpect(jsonPath("$.token_endpoint").exists())
                .andExpect(jsonPath("$.jwks_uri").exists())
                .andExpect(jsonPath("$.registration_endpoint").exists());
    }

    @Test
    void redirectsUnauthenticatedAuthorizationRequestToGoogleLogin() throws Exception {
        // A valid (PKCE) authorization request from a browser without a session is redirected into
        // the federated Google login by the AS chain's exception entry point.
        mockMvc.perform(get("/oauth2/authorize"
                        + "?response_type=code"
                        + "&client_id=mcp-demo-client"
                        + "&redirect_uri=http://127.0.0.1:8097/callback"
                        + "&scope=openid"
                        + "&code_challenge=E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM"
                        + "&code_challenge_method=S256")
                        .accept("text/html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location",
                        org.hamcrest.Matchers.containsString("/oauth2/authorization/google")));
    }

    @Test
    void redirectsGoogleAuthorizationToAccountsGoogleCom() throws Exception {
        mockMvc.perform(get("/oauth2/authorization/google"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("https://accounts.google.com/**"));
    }

    @Test
    void publishesAJwkKey() throws Exception {
        mockMvc.perform(get("/oauth2/jwks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keys[0].kty").value("RSA"));
    }

    /**
     * Minimal boot context: the three auth config classes + a mocked {@link AccountService} (so the
     * token customizer bean wires without the JPA layer). DataSource/JPA auto-config is excluded so
     * the test needs no database.
     */
    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            DataSourceTransactionManagerAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class
    })
    @Import({AuthorizationServerConfig.class, DefaultSecurityConfig.class, TokenConfig.class})
    static class TestConfig {

        @Bean
        AccountService accountService() {
            return Mockito.mock(AccountService.class);
        }
    }
}
