package org.mpashka.totemftc.api;

import io.smallrye.mutiny.Uni;
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


    /**
     * /home/m_pashka/Projects/tests/google/google-api-java-client-samples/oauth2-cmdline-sample/description.txt
     *
     * https://developers.facebook.com/docs/facebook-login/manually-build-a-login-flow
     * https://www.facebook.com/.well-known/openid-configuration
     *
     * https://graph.facebook.com/v12.0/oauth/access_token?
     *    client_id={app-id}
     *    &redirect_uri={redirect-uri}
     *    &client_secret={app-secret}
     *    &code={code-parameter}
     *
     * code=AQBl7kRQjxmtm4vf50mHaZH3F_lAUvtxak-fkj2UgSgZUL5jnwAWTHXmd-6I7LpJg9K8EqSmyCEPsGCUWzrCxi977AIDWTamBXAEznjJLuqUntahCWM5d4AVFs60PbGeFuvUATfeO0b0RsKof2H5UqaiUZhJYNv2w6QL6P5DMQcSZmCiXkPdCzywETcuLyM4x_Tad7ph7CSlrWgQ_BGIqPz2L-eaWnxZqD5h0hkK-tb6271sDYcWobE25mrOrmLEkuF0wpgmLkmNy-9C_eCWmAUAVUP6N6b0kYoAGsChpl7VgHE5B5htFJVn2KF-zJuEy5AotxYYeGdMd9c7JlrtbGR_Wal8LTC89ikhoaGAacH64QuVqXG62tM_YrFyz7jDnHS90rbuIGe3SZqGba2z0_mS
     * Response: http://localhost:8080/login/callback?code=<code>&state=my_awsome_state
     *
     * "https://graph.facebook.com/oauth/access_token"
     * "https://graph.facebook.com/v12.0/oauth/access_token"
     *
     * https://developers.facebook.com/docs/permissions/reference
     * https://developers.facebook.com/tools/explorer
     *
     */
    private static final OidcProvider facebook = new OidcProvider("facebook", "558989508631164", "a97470242f3bc315fcc69494fc831601",
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

    public Uni<UserEntity> findUser(String provider, String id, String email, String phone) {

    }

    public String getUserId() {
        return requestParameters.getUserInfo();
    }

    public void setUserInfo(UserInfo userInfo) {
        requestParameters.setUserInfo(userInfo);
    }



    /*
            public void setUserInfo(UserInfo userInfo) {
                session.s;
            }
    */

    public OidcProvider getOidcProvider(String name) {
        return providers.get(name);
    }

    /*
    Offline_access
     */
    public static class OidcProvider {
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
            return secret;
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

        public UserInfo getUserInfo() {
            return Optional.of(session).map(s -> s.<String>getParameter(USER_ID_ATTR)).orElse(null);
        }

        public void setUserInfo(UserInfo userInfo) {
            session.setParameter(USER_ID_ATTR, userInfo);
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

        public <T> void setParameter(String name, T value) {
            parameters.put(name, value);
        }
    }

}
