package org.mpashka.totemftc.api;

import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.ext.web.client.WebClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.RestCookie;
import org.jboss.resteasy.reactive.RestPath;
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

@Path("/login")
public class WebResourceLogin {

    private static final Logger log = LoggerFactory.getLogger(WebResourceLogin.class);

    private static final String STATE_ATTR = "login:state";
    private static final String SESSION_ID_COOKIE = "sessionId";

    private Map<String, AuthProvider> authProviders;

    @Inject
    SecurityService securityService;

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
    public Response initLogin(@RestPath String providerName) {
        log.debug("Init login {}.", providerName);
        // todo check if user is logged in already - abort
        SecurityService.Session session = securityService.createSession();
        AuthProvider provider = authProviders.get(providerName);
        if (provider == null) {
            throw new RuntimeException("Unknown security provider " + providerName);
        }
        String state = provider.getName() + "_" + Utils.generateRandomString(20);
        session.setParameter(STATE_ATTR, new LoginState(provider, state));
        log.debug("Session: {}", session);
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

        SecurityService.Session session;
        LoginState loginState;
        if (sessionId == null || (session = securityService.removeSession(sessionId)) == null || (loginState = session.getParameter(STATE_ATTR)) == null) {
            throw new LoginException("Session not found");
        }

        /* error_reason, error, error_description */
        String error = uriInfo.getQueryParameters().getFirst("error");
        if (error != null) {
            String errorReason = uriInfo.getQueryParameters().getFirst("error_reason");
            String errorDescription = uriInfo.getQueryParameters().getFirst("error_description");
            throw new LoginException(error, errorReason, errorDescription);
        }

        String callbackState = uriInfo.getQueryParameters().getFirst("state");
        if (!callbackState.equals(loginState.getState()) || !providerName.equals(loginState.getProvider().getName())) {
            throw new LoginException("Received invalid state");
        }

        AuthProvider authProvider = loginState.getProvider();
        return authProvider.processCallback(uriInfo, loginState)
                .onItem().transformToUni(u -> authProvider.readUserInfo(loginState))
                .onItem().invoke(loginState::setAuthUserInfo)
                .onItem().transformToUni(u -> dbUser.findById(authProvider.getName(), u.getId(), u.getEmail(), u.getPhone()))
                .onItem().transformToUni(searchResult -> {
                    if (searchResult != null && searchResult.getType() == DbUser.UserSearchType.socialNetwork) {
                        // Login by social network id - user id is present, no need to update database
                        return Uni.createFrom().item(searchResult.getUserId());
                    }

                    Uni<Integer> userIdUni = addOrUpdateUser(searchResult, loginState);
                    return loadImage(userIdUni, loginState.getAuthUserInfo().getImageUrl());
                }).onItem().transform(userId -> {
                    SecurityService.Session newSession = securityService.createSession();
                    newSession.setUserId(userId);

                    // return "<script>onLoginCompleted();</script>";
                    // probably temporary solution to avoid cross browser issue
                    return Response.status(Response.Status.FOUND)
//                            .location(URI.create("http://localhost:8081/callback/login-ok.html?session_id=<session_id>&user_type=<user_type>"
                            .location(URI.create("<frontend_url>/login-ok?session_id=<session_id>&user_type=<user_type>"
                                    .replace("<frontend_url>", frontendUrl)
                                    .replace("<session_id>", session.getSessionId())
                                    .replace("<user_type>", loginState.isNewUser() ? "new" : "existing")
                            ))
                            .cookie(new NewCookie(SESSION_ID_COOKIE,  null, "/", null, null, 0, false, false))
                            .build();
                });
    }

    private Uni<Integer> addOrUpdateUser(DbUser.UserSearchResult searchResult, LoginState loginState) {
        AuthProvider.UserInfo userInfo = loginState.getAuthUserInfo();
        if (searchResult != null) {
            // User was found by phone or email - Add social network info
            return dbUser.addSocialNetwork(searchResult.getUserId(), loginState.getProvider().getName(), userInfo.getId(), userInfo.getLink(), userInfo.getEmail(), userInfo.getPhone())
                    .onItem().transform(u -> searchResult.getUserId());
        } else {
            loginState.setNewUser(true);
            // User was not found - Create new user id
            return dbUser.addUser(new DbUser.EntityUser()
                    .setFirstName(userInfo.getFirstName())
                    .setLastName(userInfo.getLastName())
                    .setType(DbUser.UserType.guest)
            ).onItem().transformToUni(userId ->
                    dbUser.addEmail(userId, userInfo.getEmail())
                            .onItem().transform(u -> userId)
            ).onItem().transformToUni(userId ->
                    dbUser.addPhone(userId, userInfo.getPhone())
                            .onItem().transform(u -> userId)
            );
        }
    }

    private Uni<Integer> loadImage(Uni<Integer> userIdUni, String imageUrl) {
        if (!Utils.isEmpty(imageUrl)) {
            // Load image and save to db
            userIdUni = userIdUni.onItem().transformToUni(userId ->
                    webClient.getAbs(imageUrl).send()
                            .onItem().transformToUni(image -> {
                                String contentType = image.getHeader(HttpHeaders.CONTENT_TYPE);
                                return dbUser.addImage(userId, image.body(), contentType)
                                        .onItem().transformToUni(imageId -> dbUser.setMainImage(userId, imageId, true));
                            }).onItem().transform(u -> userId));
        }
        return userIdUni;
    }

    @GET
    @Path("user")
    @Produces(MediaType.APPLICATION_JSON)
    @Authenticated
    public Uni<DbUser.EntityUser> user() {
        int userId = securityService.getUserId();
        return dbUser.getUser(userId);
    }

    private static class LoginException extends RuntimeException {
        private String[] uiMessage;
        public LoginException(String... uiMessage) {
            super(Arrays.toString(uiMessage));
        }

        public String[] getUiMessage() {
            return uiMessage;
        }
    }

    public static class LoginState {
        private AuthProvider provider;
        private String state;
        private String token;
        private AuthProvider.UserInfo authUserInfo;
        private boolean newUser;

        public LoginState(AuthProvider provider, String state) {
            this.state = state;
            this.provider = provider;
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

        public boolean isNewUser() {
            return newUser;
        }

        public void setNewUser(boolean newUser) {
            this.newUser = newUser;
        }
    }

}
