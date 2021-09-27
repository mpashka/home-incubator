package org.mpashka.totemftc.api;

import io.smallrye.config.PropertiesConfigSource;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.spi.ConfigSource;
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

    private static final Map<String, OidcProvider> providers = new HashMap<>();

//    Executor executor = Infrastructure.getDefaultWorkerPool();

    private final OidcProvider facebook = new OidcProvider("facebook", "558989508631164", "",
            "openid+email+public_profile+user_gender+user_link+user_birthday+user_location",
            "https://www.facebook.com/dialog/oauth?client_id=<client_id>&redirect_uri=<redirect_uri>&state=<state>&response_type=code&scope=<scope>",
            "https://graph.facebook.com/oauth/access_token", "http://localhost:8080/login/callback");

    private ConcurrentMap<String, Session> sessions = new ConcurrentHashMap<>();

    @Inject
    Utils utils;

    @Inject
    DBUser dbUser;

    @Inject
    SecurityService.RequestParameters requestParameters;

    public SecurityService() {
    }

    @Produces
    @RequestScoped
    RequestParameters requestParameters() {
        return new RequestParameters();
    }

    public Session getSession() {
        return requestParameters.getSession();
    }

    public Session getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
    }

    public Session createSession() {
        String sessionId = utils.generateRandomString(20);
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

    public OidcProvider getOidcProvider(String name) {
        return providers.get(name);
    }

    public class OidcProvider {
        private String name;
        private String clientId;
        private String secret;
        private String scope;
        private String authorizationEndpoint;
        private String tokenEndpoint;
        private String redirectUri;

        public OidcProvider(String name, String clientId, String secret, String scope, String authorizationEndpoint, String tokenEndpoint, String redirectUri) {
            this.name = name;
            this.clientId = clientId;
            this.secret = secret;
            this.scope = scope;
            this.authorizationEndpoint = authorizationEndpoint;
            this.tokenEndpoint = tokenEndpoint;
            this.redirectUri = redirectUri;
            providers.put(name, this);
        }

        public String getAuthorizationEndpoint(String state) {
            return authorizationEndpoint
                    .replaceAll("<client_id>", clientId)
                    .replaceAll("<redirect_uri>", redirectUri)
                    .replaceAll("<state>", state)
                    .replaceAll("<scope>", scope);
        }

        public String getName() {
            return name;
        }

        public String getClientId() {
            return clientId;
        }

        public String getSecret() {
            Config config = ConfigProvider.getConfig();
            for (ConfigSource configSource : config.getConfigSources()) {
                log.info("Config source: {}", configSource);
/*
                if (configSource instanceof PropertiesConfigSource) {
                    PropertiesConfigSource propertiesConfigSource = (PropertiesConfigSource) configSource;
//                    propertiesConfigSource.
                }
*/
            }

            return ConfigProvider.getConfig().getValue("oidc.provider.facebook.secret", String.class);
//            return secret;
        }

        public String getTokenEndpoint() {
            return tokenEndpoint;
        }

        public String getRedirectUri() {
            return redirectUri;
        }
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
