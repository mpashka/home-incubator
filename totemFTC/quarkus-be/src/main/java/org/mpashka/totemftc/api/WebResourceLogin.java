package org.mpashka.totemftc.api;

import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.ext.web.client.WebClient;
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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/api/login")
public class WebResourceLogin {

    private static final Logger log = LoggerFactory.getLogger(WebResourceLogin.class);

    private Map<String, AuthProvider> authProviders;

    @Inject
    WebSessionService webSessionService;

    @Inject
    DbUser dbUser;

    @Inject
    WebClient webClient;


    @Inject
    void init(Instance<AuthProvider> authProviders) {
        this.authProviders = authProviders.stream().collect(Collectors.toMap(AuthProvider::getName, p -> p));
    }

    /**
     * @deprecated Now client is responsible for sending request to oidc and providing response callback
     * @return redirect to oidc provider login page
     */
    @GET
    @Path("init/{providerName}")
    @Produces(MediaType.TEXT_HTML)
    @Deprecated
    public Response initLogin(@RestPath String providerName) {
        log.debug("Init login {}.", providerName);
        AuthProvider provider = authProviders.get(providerName);
        if (provider == null) {
            throw new RuntimeException("Unknown login provider " + providerName);
        }
        return Response.status(Response.Status.FOUND)
                .location(URI.create(provider.getAuthorizationEndpoint()
                        .replace("<nonce>", Utils.generateRandomString(15, Utils.RANDOM_CHARS))
                ))
                .build();
    }

    /**
     * todo proper error handling
     * Response params: state&code&scope
     * @param client client id used to create appropriate redirectUrl
     */
    @GET
    @Path("loginCallback/{providerName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<LoginResult> onLoginCallback(UriInfo uriInfo, @RestPath String providerName,
                                          @RestQuery("client") String client) {
        try {
            return processCallback(uriInfo, providerName, ProviderAction.login, client, null);
        } catch (Exception e) {
            log.error("Login callback error", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * todo proper error handling
     * Response params: state&code&scope
     * @param client client id used to create appropriate redirectUrl
     */
    @GET
    @Path("linkCallback/{providerName}")
    @Authenticated
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<LoginResult> onLinkCallback(UriInfo uriInfo, @RestPath String providerName,
                                          @RestQuery("client") String client) {
        try {
            return processCallback(uriInfo, providerName, ProviderAction.link, client, webSessionService.getSession());
        } catch (Exception e) {
            log.error("Login callback error", e);
            throw new RuntimeException(e);
        }
    }

    private Uni<LoginResult> processCallback(UriInfo uriInfo, String providerName, ProviderAction action, String client, WebSessionService.Session linkSession) {
        log.info("Callback: {}. Action: {}. Client: {}, LinkSession: {}", uriInfo.getRequestUri(), action, client, linkSession);

        /* error_reason, error, error_description */
        String error = uriInfo.getQueryParameters().getFirst("error");
        if (error != null) {
            String errorReason = uriInfo.getQueryParameters().getFirst("error_reason");
            String errorDescription = uriInfo.getQueryParameters().getFirst("error_description");
            throw new LoginException(error, errorReason, errorDescription);
        }

        AuthProvider authProvider = authProviders.get(providerName);
        LoginState loginState = new LoginState();
        return authProvider.processCallback(uriInfo, loginState, client)
                .onFailure().transform(e -> new RuntimeException("Error processing callback", e))
                .onItem().transformToUni(u -> authProvider.readUserInfo(loginState))
//                .onFailure().transform(e -> new RuntimeException("Error parsing user info", e))
                .onItem().invoke(loginState::setAuthUserInfo)
                .onItem().transformToUni(u -> switch (action) {
                    case login -> userLogin(loginState);
                    case link -> userLink(linkSession, loginState);
                })
                .onItem().invoke(user -> loginState.user = user)
                .onItem().transformToUni(u -> saveImage(loginState))
                .onItem().transformToUni(imageId -> switch (action) {
                    case login -> webSessionService.createSession(loginState.user);
                    case link -> Uni.createFrom().item(linkSession);
                })
                .onItem().invoke(session -> loginState.session = session)
                .onItem().transform(u -> new LoginResult(loginState.session.getSessionId(), loginState.newUser ? "newUser" : "existing"));
    }

    /**
     * find user for login scenario, add social network if user was found by e-mail or by phone,
     * @return user id
     */
    private Uni<DbUser.EntityUser> userLogin(LoginState loginState) {
        AuthProvider.UserInfo userInfo = loginState.getAuthUserInfo();
        return dbUser.findById(userInfo)
                .onItem().transformToUni(searchResult -> {
                    if (searchResult != null && searchResult.getType() == DbUser.UserSearchType.socialNetwork) {
                        // Login by social network id - user id is present, no need to update database
                        loginState.loadImage = false;
                        return Uni.createFrom().item(searchResult.getUser());
                    } else if (searchResult != null) {
                        // User was found by phone or email - Add social network info
                        loginState.loadImage = true;
                        DbUser.EntityUser user = searchResult.getUser();
                        return dbUser.addUserSocialNetwork(user.getUserId(), userInfo)
                                        .onItem().transform(u -> user);
                    } else {
                        // User was not found - Create new user
                        loginState.newUser = true;
                        loginState.loadImage = true;
                        DbUser.EntityUser user = new DbUser.EntityUser()
                                .setFirstName(userInfo.getFirstName())
                                .setLastName(userInfo.getLastName())
                                .setTypes(EnumSet.noneOf(DbUser.UserType.class));
                        return dbUser.addUser(user)
                                .onItem().transformToUni(u ->
                                        dbUser.addUserSocialNetwork(u.getUserId(), userInfo)
                                                .onItem().transform(u_ -> user));
                    }
                });
    }

    /**
     * saves user social network info to DB
     * @return user id
     */
    private Uni<DbUser.EntityUser> userLink(WebSessionService.Session session, LoginState loginState) {
        AuthProvider.UserInfo userInfo = loginState.getAuthUserInfo();
        DbUser.EntityUser user = session.getUser();
        return dbUser.addUserSocialNetwork(user.getUserId(), userInfo)
                .onItem().transform(u -> user);
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
        return webClient
                .getAbs(imageUrl)
                .timeout(3000)
                .send()
                .onItem().transformToUni(image -> {
                    String contentType = image.getHeader(HttpHeaders.CONTENT_TYPE);
                    int userId = loginState.user.getUserId();
                    return dbUser.addImage(userId, image.body(), contentType)
                            .onItem().transformToUni(imageId ->
                                    dbUser.setMainImage(userId, imageId, true)
                                            .onItem().transform(u -> imageId));
                })
                .onFailure().recoverWithItem(t -> {
                    log.error("Error loading image {}", imageUrl, t);
                    return 0;
                });
    }

    @GET
    @Path("logout")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> logout() {
        return webSessionService.closeSession()
                .onItem().transform(u -> Response.ok().build());
    }

    /**
     * JSON entity returning to client
     */
    static class LoginResult {
        private String sessionId;
        private String userType;

        public LoginResult(String sessionId, String userType) {
            this.sessionId = sessionId;
            this.userType = userType;
        }
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

    /**
     * Internal class to store login processing state
     */
    static class LoginState {
        private String token;
        private AuthProvider.UserInfo authUserInfo;
        private WebSessionService.Session session;
        private DbUser.EntityUser user;
        /** true if this is new user, and we are adding him into db */
        private boolean newUser;
        /** true if this is new user or new social network, and thus we need to load social network image */
        private boolean loadImage;

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
