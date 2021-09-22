package org.mpashka.totemftc.api;

import org.jboss.resteasy.reactive.server.ServerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.security.Principal;

//@Provider
//@PreMatching
public class SecurityFilter /*implements ContainerRequestFilter*/ {

    private static final Logger log = LoggerFactory.getLogger(SecurityFilter.class);

    @Inject
    SecurityService securityService;

    @Inject
    SecurityService.RequestParameters requestParameters;

    @ServerRequestFilter(preMatching = true)
    public void filter(ContainerRequestContext requestContext) {
        String auth = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        log.debug("Auth: {}", auth);
        if (auth == null || !auth.startsWith("Bearer ")) {
            return;
        }
        SecurityService.Session session = securityService.getSession(auth.substring(7));
        log.debug("Session: {}", session);
        if (session == null) {
            return;
        }
        requestParameters.setSession(session);
        SecurityService.UserInfo userInfo = requestParameters.getUserInfo();
        log.debug("UserInfo: {}", userInfo);
        if (userInfo == null) {
            return;
        }
        requestContext.setSecurityContext(new SecurityContext() {
            @Override
            public Principal getUserPrincipal() {
                return () -> "user";
            }

            @Override
            public boolean isUserInRole(String role) {
                log.trace("Check if user is in role {}", role);
                return role.equals("user");
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
    }
}
