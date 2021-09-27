package org.mpashka.totemftc.api;

import io.quarkus.security.credential.Credential;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.security.Permission;
import java.security.Principal;
import java.util.Map;
import java.util.Set;

//@Provider
//@PreMatching
public class SecurityFilter /*implements ContainerRequestFilter*/ {

    private static final Logger log = LoggerFactory.getLogger(SecurityFilter.class);

    private static final SecurityIdentity SECURITY_EMPTY = new SecurityIdentity() {
        @Override
        public Principal getPrincipal() {
            return null;
        }

        @Override
        public boolean isAnonymous() {
            return true;
        }

        @Override
        public Set<String> getRoles() {
            return null;
        }

        @Override
        public boolean hasRole(String role) {
            return false;
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
            return null;
        }
    };

    private static final String USER = "user";
    private static final Principal PRINCIPAL = () -> USER;
    private static final Set<String> ROLES = Set.of(USER);
    private static final SecurityIdentity SECURITY_SECURE = new SecurityIdentity() {
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

    @Inject
    SecurityService.RequestParameters requestParameters;

    @Inject
    CurrentIdentityAssociation identityAssociation;

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
        Integer userId = requestParameters.getUserId();
        log.debug("UserId: {}", userId);
        if (userId == null) {
            return;
        }
        requestContext.setSecurityContext(new SecurityContext() {
            @Override
            public Principal getUserPrincipal() {
                return PRINCIPAL;
            }

            @Override
            public boolean isUserInRole(String role) {
                log.trace("Check if user is in role {}", role);
                return role.equals(USER);
            }

            @Override
            public boolean isSecure() {
                log.trace("Check if is secure");
                return true;
            }

            @Override
            public String getAuthenticationScheme() {
                return "token";
            }
        });
        identityAssociation.setIdentity(SECURITY_SECURE);
    }
}
