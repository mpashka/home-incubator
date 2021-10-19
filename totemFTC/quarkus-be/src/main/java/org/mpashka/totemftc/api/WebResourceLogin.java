package org.mpashka.totemftc.api;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.ext.web.client.WebClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.RestCookie;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/api/login")
public class WebResourceLogin {

    private static final Logger log = LoggerFactory.getLogger(WebResourceLogin.class);

    private static final String STATE_ATTR = "login:state";
    private static final String SESSION_ID_COOKIE = "sessionId";

    private Map<String, AuthProvider> authProviders;

    @Inject
    WebSessionService webSessionService;

    @Inject
    DbUser dbUser;

    @Inject
    @ConfigProperty(name = "app.url.frontend", defaultValue = "http://localhost:8081")
    String frontendUrl;

    @Inject
    WebClient webClient;


    @Inject
    void init(Instance<AuthProvider> authProviders) {
        this.authProviders = authProviders.stream().collect(Collectors.toMap(AuthProvider::getName, p -> p));
    }

    /**
     * @return redirect to oidc provider login page
     */
    @GET
    @Path("init/{providerName}")
    @Produces(MediaType.TEXT_HTML)
    public Response initLogin(@RestPath String providerName, @RestQuery("action") ProviderAction action, @RestQuery("sessionId") String sessionId) {
        log.debug("Init login {} / {}.", providerName, action);
        WebSessionService.Session session;
        switch (action) {
            case link:
                session = webSessionService.getSession(sessionId);
                if (session == null) {
                    throw new RuntimeException("Session not found " + sessionId);
                }
                break;

            default:
                action = ProviderAction.login;
            case login:
                if (webSessionService.getSession() != null) {
                    throw new RuntimeException("User already logged in");
                }
                session = webSessionService.createSession();
                break;
        }
        AuthProvider provider = authProviders.get(providerName);
        if (provider == null) {
            throw new RuntimeException("Unknown security provider " + providerName);
        }
        String state = provider.getName() + "_" + Utils.generateRandomString(20);
        session.setParameter(STATE_ATTR, new LoginState(action, provider, state));
        log.debug("Pre-login session: {}", session);
        return Response.status(Response.Status.FOUND)
                .location(URI.create(provider.getAuthorizationEndpoint()
                        .replace("<state>", state)
                        .replace("<nonce>", Utils.generateRandomString(15))
                ))
                .cookie(new NewCookie(SESSION_ID_COOKIE, session.getSessionId(), "/", null, null, NewCookie.DEFAULT_MAX_AGE, false))
                .build();
    }

    /**
     * todo proper error handling
     */
    @GET
    @Path("callback/{providerName}")
    @Produces(MediaType.TEXT_HTML)
    public Uni<Response> callback(UriInfo uriInfo, @RestPath String providerName, @RestCookie(SESSION_ID_COOKIE) String sessionId) {
        try {
            return processCallback(uriInfo, providerName, sessionId);
        } catch (Exception e) {
            log.error("Login callback error", e);
            throw new RuntimeException(e);

        /*
            return Uni.createFrom().item(
                    Response
                            .status(Response.Status.BAD_REQUEST)
                            .entity("<!DOCTYPE html><html><body>" +
                                    "<div>Session cookie is not set</div>" +
                                    "</body></html>")
                            .build()
            );
        
         */
        }
    }

    private Uni<Response> processCallback(UriInfo uriInfo, String providerName, String sessionId) {
        log.info("Callback: {}. SessionId: {}", uriInfo.getRequestUri(), sessionId);
        // Response params: state&code&scope

        WebSessionService.Session session;
        LoginState loginState;
        if (sessionId == null || (session = webSessionService.getSession(sessionId)) == null || (loginState = session.removeParameter(STATE_ATTR)) == null) {
            throw new LoginException("Session not found");
        }
        if (loginState.action == ProviderAction.login) {
            webSessionService.removeSession(sessionId);
        }

        /* error_reason, error, error_description */
        String error = uriInfo.getQueryParameters().getFirst("error");
        if (error != null) {
            String errorReason = uriInfo.getQueryParameters().getFirst("error_reason");
            String errorDescription = uriInfo.getQueryParameters().getFirst("error_description");
            throw new LoginException(error, errorReason, errorDescription);
        }

        AuthProvider authProvider = loginState.getProvider();
        return authProvider.processCallback(uriInfo, loginState)
                .onFailure().transform(e -> new RuntimeException("Error processing callback", e))
                .onItem().transformToUni(u -> authProvider.readUserInfo(loginState))
//                .onFailure().transform(e -> new RuntimeException("Error parsing user info", e))
                .onItem().invoke(loginState::setAuthUserInfo)
                .onItem().transformToUni(u -> {
                    switch (loginState.action) {
                        case login: return userIdLogin(loginState);
                        case link: return userIdLink(session, loginState);
                        default: throw new RuntimeException("Unknown action " + loginState.action);
                    }
                })
                .onItem().invoke(userId -> loginState.userId = userId)
                .onItem().transformToUni(u -> saveImage(loginState))
                .onItem().transform(imageId -> {
                    WebSessionService.Session newSession;
                    switch (loginState.action) {
                        case login:
                            newSession = webSessionService.createSession();
                            newSession.setUserId(loginState.userId);
                            log.debug("Login session: {}, UserId: {}", newSession.getSessionId(), newSession.getUserId());
                            break;

                        case link:
                            newSession = session;
                            break;
                        default: throw new RuntimeException("Unknown action " + loginState.action);
                    }

                    // return "<script>onLoginCompleted();</script>";
                    // probably temporary solution to avoid cross browser issue
                    return Response.status(Response.Status.FOUND)
//                            .location(URI.create("http://localhost:8081/callback/login-ok.html?session_id=<session_id>&user_type=<user_type>"
                            .location(URI.create("<frontend_url>/login/ok?action=<action>&session_id=<session_id>&user_type=<user_type>"
                                    .replace("<action>", loginState.action.name())
                                    .replace("<frontend_url>", frontendUrl)
                                    .replace("<session_id>", newSession.getSessionId())
                                    .replace("<user_type>", loginState.newUser ? "new" : "existing")
                            ))
                            .cookie(new NewCookie(SESSION_ID_COOKIE,  null, "/", null, null, 0, false, false))
                            .build();
                });
    }

    /**
     * find user for login scenario, add social network if user was found by e-mail or by phone,
     * @return user id
     */
    private Uni<Integer> userIdLogin(LoginState loginState) {
        AuthProvider.UserInfo userInfo = loginState.getAuthUserInfo();
        return dbUser.findById(userInfo)
                .onItem().transformToUni(searchResult -> {
                    if (searchResult != null && searchResult.getType() == DbUser.UserSearchType.socialNetwork) {
                        // Login by social network id - user id is present, no need to update database
                        loginState.loadImage = false;
                        return Uni.createFrom().item(searchResult.getUserId());
                    } else if (searchResult != null) {
                        // User was found by phone or email - Add social network info
                        loginState.loadImage = true;
                        return
                                dbUser.addUserSocialNetwork(searchResult.getUserId(), userInfo)
                                        .onItem().transform(u -> searchResult.getUserId());
                    } else {
                        // User was not found - Create new user id
                        loginState.newUser = true;
                        loginState.loadImage = true;
                        return
                                dbUser.addUser(new DbUser.EntityUser()
                                        .setFirstName(userInfo.getFirstName())
                                        .setLastName(userInfo.getLastName())
                                        .setType(DbUser.UserType.guest)
                                ).onItem().transformToUni(userId ->
                                        dbUser.addUserSocialNetwork(userId, userInfo)
                                                .onItem().transform(u -> userId)
                                );
                    }
                });
    }

    /**
     * saves user social network info to DB
     * @return user id
     */
    private Uni<Integer> userIdLink(WebSessionService.Session session, LoginState loginState) {
        AuthProvider.UserInfo userInfo = loginState.getAuthUserInfo();
        int userId = session.getUserId();
        return dbUser.addUserSocialNetwork(userId, userInfo)
                .onItem().transform(u -> userId);
    }

    /**
     * Loads image from social network and saves it into database
     * @return image id
     */
    private Uni<Integer> saveImage(LoginState loginState) {
        String imageUrl = loginState.getAuthUserInfo().getImageUrl();
        if (!loginState.loadImage || Utils.isEmpty(imageUrl)) {
            return Uni.createFrom().item(0);
        }

        // Load image and save to db
        return webClient.getAbs(imageUrl).send()
                .onItem().transformToUni(image -> {
                    String contentType = image.getHeader(HttpHeaders.CONTENT_TYPE);
                    int userId = loginState.userId;
                    return dbUser.addImage(userId, image.body(), contentType)
                            .onItem().transformToUni(imageId ->
                                    dbUser.setMainImage(userId, imageId, true)
                                            .onItem().transform(u -> imageId));
                });
    }

    @GET
    @Path("logout")
    @Produces(MediaType.APPLICATION_JSON)
    public Response logout() {
        webSessionService.closeSession();
        return Response.ok().build();
    }

    static class LoginException extends RuntimeException {
        private String[] uiMessage;
        public LoginException(String... uiMessage) {
            super(Arrays.toString(uiMessage));
        }

        public String[] getUiMessage() {
            return uiMessage;
        }
    }

    static class LoginState {
        private ProviderAction action;
        private AuthProvider provider;
        private String state;
        private String token;
        private AuthProvider.UserInfo authUserInfo;
        private int userId;
        /** true if this is new user and we add him into db */
        private boolean newUser;
        /** true if this is new user or new social network */
        private boolean loadImage;

        public LoginState(ProviderAction action, AuthProvider provider, String state) {
            this.action = action;
            this.provider = provider;
            this.state = state;
        }

        public ProviderAction getAction() {
            return action;
        }

        public AuthProvider getProvider() {
            return provider;
        }

        public String getState() {
            return state;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public AuthProvider.UserInfo getAuthUserInfo() {
            return authUserInfo;
        }

        public void setAuthUserInfo(AuthProvider.UserInfo authUserInfo) {
            this.authUserInfo = authUserInfo;
        }
    }

    public enum ProviderAction {
        login, link
    }
}
