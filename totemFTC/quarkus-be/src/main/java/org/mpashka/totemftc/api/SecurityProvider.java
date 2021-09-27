package org.mpashka.totemftc.api;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.security.credential.Credential;
import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.AuthenticationRequest;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.quarkus.vertx.http.runtime.security.HttpCredentialTransport;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.SecurityContext;
import java.security.Permission;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * https://quarkus.io/guides/security
 * https://quarkus.io/guides/security-customization
 * https://quarkus.io/guides/security-authorization
 * https://quarkus.io/guides/security-built-in-authentication
 */
@Singleton
public class SecurityProvider implements HttpAuthenticationMechanism {

    private static final Logger log = LoggerFactory.getLogger(SecurityProvider.class);

    private static final String USER = "user";
    private static final Principal PRINCIPAL = () -> USER;
    private static final Set<String> ROLES = Set.of(USER);
    private static final SecurityIdentity SECURE = new SecurityIdentity() {
        @Override
        public Principal getPrincipal() {
            return PRINCIPAL;
        }

        @Override
        public boolean isAnonymous() {
            return false;
        }

        @Override
        public Set<String> getRoles() {
            return ROLES;
        }

        @Override
        public boolean hasRole(String role) {
            return ROLES.contains(role);
        }

        @Override
        public <T extends Credential> T getCredential(Class<T> credentialType) {
            return null;
        }

        @Override
        public Set<Credential> getCredentials() {
            return null;
        }

        @Override
        public <T> T getAttribute(String name) {
            return null;
        }

        @Override
        public Map<String, Object> getAttributes() {
            return null;
        }

        @Override
        public Uni<Boolean> checkPermission(Permission permission) {
            return Uni.createFrom().item(permission.getName().equals(USER));
        }
    };

    @Inject
    SecurityService securityService;

    @Override
    public Uni<SecurityIdentity> authenticate(RoutingContext context, IdentityProviderManager identityProviderManager) {
        String auth = context.request().getHeader(HttpHeaders.AUTHORIZATION);
        log.debug("Auth[{}]: {}", context.request().uri(), auth);
        if (auth == null || !auth.startsWith("Bearer ")) {
            return Uni.createFrom().optional(Optional.empty());
        }
        SecurityService.Session session = securityService.getSession(auth.substring(7));
        log.debug("Session: {}", session);
        if (session == null) {
            return Uni.createFrom().optional(Optional.empty());
        }
        Integer userId = session.getUserId();
        log.debug("UserId: {}", userId);
        if (userId == null) {
            return Uni.createFrom().optional(Optional.empty());
        }
        return Uni.createFrom().item(SECURE);
    }

    @Override
    public Uni<ChallengeData> getChallenge(RoutingContext context) {
        log.info("::getChallenge");
        ChallengeData challengeData = new ChallengeData(HttpResponseStatus.FORBIDDEN.code(), null, null);
        return Uni.createFrom().item(challengeData);
    }

    @Override
    public Set<Class<? extends AuthenticationRequest>> getCredentialTypes() {
        log.info("::getCredentialTypes");
        return Collections.emptySet();
    }

    @Override
    public HttpCredentialTransport getCredentialTransport() {
        log.info("::getCredentialTransport");
        return new HttpCredentialTransport(HttpCredentialTransport.Type.AUTHORIZATION, "token");
    }

    /**
     * Set session to request parameter.
     * Is needed since {@link HttpAuthenticationMechanism} doesn't have access to request context
     */
    @Singleton
    public static class SecurityFilter implements ContainerRequestFilter {

        @Inject
        SecurityService.RequestParameters requestParameters;

        @Inject
        SecurityService securityService;

        @ServerRequestFilter(preMatching = true)
        public void filter(ContainerRequestContext requestContext) {
            String auth = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
            log.debug("Auth[{}]: {}", requestContext.getUriInfo().getRequestUri(), auth);
            if (auth == null || !auth.startsWith("Bearer ")) {
                return;
            }
            SecurityService.Session session = securityService.getSession(auth.substring(7));
            log.debug("Session: {}", session);
            if (session == null) {
                return;
            }
            requestParameters.setSession(session);
        }
    }
}
