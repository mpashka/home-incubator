package org.mpashka.totemftc.api;

import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@ApplicationScoped
public class WebSessionService {

    private static final Logger log = LoggerFactory.getLogger(WebSessionService.class);

    private static final String USER_ID_ATTR = "login:userId";

//    Executor executor = Infrastructure.getDefaultWorkerPool();

    private ConcurrentMap<String, Session> sessions = new ConcurrentHashMap<>();

    @Inject
    WebSessionService.RequestParameters requestParameters;

    @RequestScoped
    RequestParameters requestParameters() {
        return new RequestParameters();
    }

    public Session getSession() {
        return requestParameters.session;
    }

    public void closeSession() {
        if (requestParameters.session != null) {
            sessions.remove(requestParameters.session.sessionId);
            requestParameters.setSession(null);
        }
    }

    public Session getSession(String sessionId) {
        return sessions.get(sessionId);
    }

//    public Uni<Session>

    public Session removeSession(String sessionId) {
        return sessions.remove(sessionId);
    }

    public Session createSession() {
        String sessionId = Utils.generateRandomString(20);
        Session session = new Session(sessionId);
        requestParameters.setSession(session);
        sessions.put(sessionId, session);
        return session;
    }

    public Integer getUserId() {
        return Optional.of(requestParameters.getSession()).map(Session::getUserId).orElse(null);
    }

    /**
     * Used to ass
     */
    public static class RequestParameters {
        private Session session;

        public Session getSession() {
            return session;
        }

        public void setSession(Session session) {
            this.session = session;
        }
    }

    public static class Session {
        private String sessionId;
        private Map<String, Object> parameters = new HashMap<>();

        public Session(String sessionId) {
            this.sessionId = sessionId;
        }

        public String getSessionId() {
            return sessionId;
        }

        public <T> T getParameter(String name) {
            return (T) parameters.get(name);
        }

        public <T> T removeParameter(String name) {
            return (T) parameters.remove(name);
        }

        public <T> void setParameter(String name, T value) {
            parameters.put(name, value);
        }

        public Integer getUserId() {
            return getParameter(USER_ID_ATTR);
        }

        public void setUserId(int userId) {
            setParameter(USER_ID_ATTR, userId);
        }
    }

    /**
     * Set session to request parameter.
     * Is needed since {@link HttpAuthenticationMechanism} doesn't have access to request context
     */
    @Singleton
    public static class MySecurityFilter implements ContainerRequestFilter {

        @Inject
        WebSessionService.RequestParameters requestParameters;

        @Inject
        WebSessionService webSessionService;

        @ServerRequestFilter(preMatching = true)
        public void filter(ContainerRequestContext requestContext) {
            String auth = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
            log.debug("Auth[{}]: {}", requestContext.getUriInfo().getRequestUri(), auth);
            if (auth == null || !auth.startsWith("Bearer ")) {
                return;
            }
            WebSessionService.Session session = webSessionService.getSession(auth.substring(7));
            log.debug("Session: {}", session);
            if (session == null) {
                return;
            }
            requestParameters.setSession(session);
        }
    }
}
