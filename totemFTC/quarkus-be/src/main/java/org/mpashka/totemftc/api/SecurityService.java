package org.mpashka.totemftc.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Produces;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@ApplicationScoped
public class SecurityService {

    private static final Logger log = LoggerFactory.getLogger(SecurityService.class);

    private static final String USER_ID_ATTR = "login:userId";

//    Executor executor = Infrastructure.getDefaultWorkerPool();

    private ConcurrentMap<String, Session> sessions = new ConcurrentHashMap<>();

    @Inject
    DbUser dbUser;

    @Inject
    SecurityService.RequestParameters requestParameters;

    @Produces
    @RequestScoped
    RequestParameters requestParameters() {
        return new RequestParameters();
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
        return requestParameters.getUserId();
    }

    public void setUserInfo(Integer userId) {
        requestParameters.setUserId(userId);
    }

    public static class RequestParameters {
        private Session session;

        public Session getSession() {
            return session;
        }

        public void setSession(Session session) {
            this.session = session;
        }

        public Integer getUserId() {
            return Optional.of(session).map(Session::getUserId).orElse(null);
        }

        public void setUserId(Integer userId) {
            session.setUserId(userId);
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

}
