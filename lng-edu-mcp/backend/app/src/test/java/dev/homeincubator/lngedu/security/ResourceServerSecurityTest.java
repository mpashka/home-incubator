// @tag:auth
package dev.homeincubator.lngedu.security;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import dev.homeincubator.lngedu.account.AccountService;
import dev.homeincubator.lngedu.auth.ResourceServerConfig;
import dev.homeincubator.lngedu.common.ForbiddenException;
import dev.homeincubator.lngedu.session.SessionCommands.SessionView;
import dev.homeincubator.lngedu.session.SessionController;
import dev.homeincubator.lngedu.session.SessionService;
import dev.homeincubator.lngedu.stats.DailyStatsView;
import dev.homeincubator.lngedu.stats.StatsService;
import dev.homeincubator.lngedu.user.ProfileController;
import dev.homeincubator.lngedu.user.ProfileService;
import dev.homeincubator.lngedu.stats.StatsController;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Resource-server security test (Phase I, {@code @tag:auth}). Loads the real
 * {@link ResourceServerConfig} filter chain over two protected controllers and asserts the whole
 * bearer-token surface behaviour without a database:
 * <ul>
 *   <li>no token → 401 with the RFC 9728 {@code WWW-Authenticate: ... resource_metadata=...} header;</li>
 *   <li>a valid mock JWT ({@code account_id} + correct audience) → 200;</li>
 *   <li>a real signed token with the correct audience → 200 (exercises the actual decoder);</li>
 *   <li>a signed token with the wrong audience → 401;</li>
 *   <li>ownership: a learner NOT owned by the token's account → 403 Problem Details; an owned learner → 200.</li>
 * </ul>
 */
@WebMvcTest({ProfileController.class, StatsController.class, SessionController.class})
@Import({ResourceServerConfig.class, CurrentAccount.class, ResourceServerSecurityTest.JwtSupport.class})
@TestPropertySource(properties = {
        "AUTH_ISSUER_URI=http://localhost:8097",
        "MCP_RESOURCE_URI=http://localhost:8097"
})
class ResourceServerSecurityTest {

    private static final String ISSUER = "http://localhost:8097";
    private static final String RESOURCE = "http://localhost:8097";
    private static final UUID ACCOUNT = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtEncoder jwtEncoder;

    @MockitoBean
    private ProfileService profileService;

    @MockitoBean
    private StatsService statsService;

    @MockitoBean
    private SessionService sessionService;

    @MockitoBean
    private AccountService accountService;

    @Test
    void noToken_returns401WithResourceMetadataChallenge() throws Exception {
        mockMvc.perform(get("/api/profiles"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, Matchers.containsString(
                        "resource_metadata=\"" + RESOURCE + "/.well-known/oauth-protected-resource\"")));
    }

    @Test
    void validMockJwt_returns200() throws Exception {
        when(profileService.listLearnersOwnedBy(ACCOUNT)).thenReturn(List.of());
        mockMvc.perform(get("/api/profiles").with(jwt().jwt(j -> j
                        .claim("account_id", ACCOUNT.toString())
                        .audience(List.of(RESOURCE)))))
                .andExpect(status().isOk());
    }

    @Test
    void validSignedToken_returns200() throws Exception {
        when(profileService.listLearnersOwnedBy(ACCOUNT)).thenReturn(List.of());
        String token = signedToken(List.of(RESOURCE));
        mockMvc.perform(get("/api/profiles").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void signedTokenWithWrongAudience_returns401() throws Exception {
        String token = signedToken(List.of("http://someone-else.example"));
        mockMvc.perform(get("/api/profiles").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void learnerNotOwnedByAccount_returns403ProblemDetails() throws Exception {
        UUID other = UUID.randomUUID();
        doThrow(new ForbiddenException("not yours"))
                .when(accountService).assertOwnsLearner(eq(ACCOUNT), eq(other));

        mockMvc.perform(get("/api/stats/daily").param("userId", other.toString())
                        .with(jwt().jwt(j -> j
                                .claim("account_id", ACCOUNT.toString())
                                .audience(List.of(RESOURCE)))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.title").value("Forbidden"))
                .andExpect(jsonPath("$.type").value("urn:problem-type:forbidden"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void ownedLearner_isAllowed() throws Exception {
        UUID owned = UUID.randomUUID();
        // assertOwnsLearner is a no-op mock (ownership passes); the stats service returns a view.
        when(statsService.getDailyStats(owned)).thenReturn(new DailyStatsView(
                LocalDate.of(2026, 7, 15), "Europe/Belgrade", 0, 0, 0, 0, 0));

        mockMvc.perform(get("/api/stats/daily").param("userId", owned.toString())
                        .with(jwt().jwt(j -> j
                                .claim("account_id", ACCOUNT.toString())
                                .audience(List.of(RESOURCE)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timezone").value("Europe/Belgrade"));
    }

    @Test
    void finishSessionOfLearnerNotOwnedByAccount_returns403() throws Exception {
        UUID sessionId = UUID.randomUUID();
        UUID otherLearner = UUID.randomUUID();
        when(sessionService.getSessionLearner(sessionId)).thenReturn(otherLearner);
        doThrow(new ForbiddenException("not yours"))
                .when(accountService).assertOwnsLearner(eq(ACCOUNT), eq(otherLearner));

        mockMvc.perform(post("/api/sessions/" + sessionId + "/finish")
                        .with(jwt().jwt(j -> j
                                .claim("account_id", ACCOUNT.toString())
                                .audience(List.of(RESOURCE)))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.type").value("urn:problem-type:forbidden"));
    }

    @Test
    void finishSessionOfOwnedLearner_isAllowed() throws Exception {
        UUID sessionId = UUID.randomUUID();
        UUID ownedLearner = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        when(sessionService.getSessionLearner(sessionId)).thenReturn(ownedLearner);
        // assertOwnsLearner is a no-op mock (ownership passes); finish returns a finished view.
        when(sessionService.finishLearningSession(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new SessionView(sessionId, ownedLearner, bookId,
                        Instant.parse("2026-07-15T10:00:00Z"),
                        Instant.parse("2026-07-15T10:20:00Z"), false));

        mockMvc.perform(post("/api/sessions/" + sessionId + "/finish")
                        .with(jwt().jwt(j -> j
                                .claim("account_id", ACCOUNT.toString())
                                .audience(List.of(RESOURCE)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sessionId.toString()))
                .andExpect(jsonPath("$.active").value(false));
    }

    private String signedToken(List<String> audience) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(ACCOUNT.toString())
                .audience(audience)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(300))
                .claim("account_id", ACCOUNT.toString())
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    /** Provides the in-process signing key (JWKSource) the resource server verifies against, plus an
     * encoder to mint real signed tokens for the audience/decoder assertions. */
    @TestConfiguration
    static class JwtSupport {

        @Bean
        JWKSource<SecurityContext> jwkSource() throws Exception {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();
            RSAKey rsaKey = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                    .privateKey((RSAPrivateKey) keyPair.getPrivate())
                    .keyID("test-key")
                    .build();
            return new ImmutableJWKSet<>(new JWKSet(rsaKey));
        }

        @Bean
        JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
            return new NimbusJwtEncoder(jwkSource);
        }
    }
}
