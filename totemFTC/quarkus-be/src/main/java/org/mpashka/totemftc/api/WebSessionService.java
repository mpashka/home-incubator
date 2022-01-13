package org.mpashka.totemftc.api;

import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.util.Optional.ofNullable;

/**
 * todo check user access rights
 */
@ApplicationScoped
public class WebSessionService {

    private static final Logger log = LoggerFactory.getLogger(WebSessionService.class);

    private static final int SESSION_TIMEOUT_DAYS = 60;
    private static final int SESSION_ID_LENGTH = 20;
    private static final int SESSION_UPDATE_HOURS = 24;

    private ConcurrentMap<String, Session> sessions = new ConcurrentHashMap<>();

    @Inject
    WebSessionService.RequestParameters requestParameters;

    @Inject
    DbUser dbUser;

    @RequestScoped
    RequestParameters requestParameters() {
        return new RequestParameters();
    }

    public Session getSession() {
        return requestParameters.getSession();
    }

    public void setSession(Session session) {
        requestParameters.setSession(session);
    }

    @Authenticated
    public Uni<Void> closeSession() {
        Session session = requestParameters.getSession();
        if (session != null) {
            sessions.remove(session.getSessionId());
            requestParameters.setSession(null);
            return dbUser.deleteSession(session)
                    .replaceWithVoid();
        } else {
            return Uni.createFrom().voidItem();
        }
    }

    public Uni<Session> fetchSession(String sessionId) {
        Session session = sessions.get(sessionId);
        if (session != null) {
            if (session.getUserId() != null) {
                if (session.getLastUpdate().isBefore(OffsetDateTime.now().minus(SESSION_UPDATE_HOURS, ChronoUnit.HOURS))) {
                    session.update();
                    log.debug("Session last update is too old. Updating...");
                    return dbUser.updateSession(session)
                            .onItem().transform(u -> session);
                }
                return Uni.createFrom().item(session);
            } else {
                log.warn("Broken session {}. User is null.", sessionId);
                return dbUser.deleteSession(session)
                        .replaceWith((Session) null);
            }
        }
        return dbUser.getSession(sessionId)
                .onItem().transformToUni(s -> {
                    if (s == null) {
                        return Uni.createFrom().nullItem();
                    }
                    Session prevSession = sessions.putIfAbsent(s.getSessionId(), s);
                    if (prevSession != null) {
                        return Uni.createFrom().item(prevSession);
                    }
                    s.update();
                    return dbUser.updateSession(s)
                            .onItem().transform(u -> s);
                });
    }

    public Session getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    public Uni<Session> createSession(DbUser.EntityUser user) {
        String sessionId = Utils.generateRandomString(SESSION_ID_LENGTH, Utils.HTTP_VALUE_RANDOM_CHARS);
        Session session = new Session(sessionId, user, OffsetDateTime.now());
        requestParameters.setSession(session);
        sessions.put(sessionId, session);
        return dbUser.createSession(session)
                .onItem().transform(u -> session);
    }

    public Uni<Void> cleanupSessions() {
        log.info("Cleanup old sessions");
        OffsetDateTime minTime = OffsetDateTime.now().minus(SESSION_TIMEOUT_DAYS, ChronoUnit.DAYS);
        sessions.values().removeIf(session -> session.getLastUpdate().isBefore(minTime));
        return dbUser.cleanupSessions(minTime)
                .onItem().invoke(i -> log.info("Cleaned up {} old sessions from db", i))
                .replaceWithVoid();
    }

    public Integer getUserId() {
        return Optional.of(requestParameters.getSession()).map(Session::getUserId).orElse(null);
    }

    public DbUser.EntityUser getUser() {
        return Optional.of(requestParameters.getSession()).map(Session::getUser).orElse(null);
    }

    /**
     * Used to associate session with request
     * @see WebSessionService#requestParameters
     */
    static class RequestParameters {
        private Session session;

        public Session getSession() {
            return session;
        }

        void setSession(Session session) {
            this.session = session;
        }
    }

    public static class Session {
        private String sessionId;
        private DbUser.EntityUser user;
        private OffsetDateTime lastUpdate;
        private Map<String, Object> parameters = new HashMap<>();

        public Session(String sessionId, DbUser.EntityUser user, OffsetDateTime lastUpdate) {
            this.sessionId = sessionId;
            this.user = user;
            this.lastUpdate = lastUpdate;
        }

        public String getSessionId() {
            return sessionId;
        }

        public DbUser.EntityUser getUser() {
            return user;
        }

        public Integer getUserId() {
            return ofNullable(user).map(DbUser.EntityUser::getUserId).orElse(null);
        }

        public OffsetDateTime getLastUpdate() {
            return lastUpdate;
        }

        public void update() {
            this.lastUpdate = OffsetDateTime.now();
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

        @Override
        public String toString() {
            return "Session{" +
                    "id='" + sessionId + '\'' +
                    ", userId='" + user.getUserId() + '\'' +
                    ", userTypes='" + user.getTypes() + '\'' +
                    ", lastUpdate=" + lastUpdate +
                    ", " + parameters +
                    '}';
        }
    }
}
