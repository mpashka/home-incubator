package org.mpashka.totemftc.api;

//import io.quarkus.security.Authenticated;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;
import io.vertx.mutiny.sqlclient.Tuple;
import org.jboss.resteasy.reactive.RestCookie;
import org.jboss.resteasy.reactive.RestPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.Optional;

@Path("/login")
public class WebResourceLogin {

    private static final Logger log = LoggerFactory.getLogger(WebResourceLogin.class);

    private static final String STATE_ATTR = "login:state";
    private static final String NEW_USER_ATTR = "login:newUser";
    private static final String SESSION_ID_COOKIE = "sessionId";

    private final WebClient client;

    @Inject
    SecurityService securityService;

    @Inject
    Utils utils;

    @Inject
    DbUser dbUser;

    @Inject
    public WebResourceLogin(Vertx vertx) {
        this.client = WebClient.create(vertx);
    }

    @GET
    @Path("init/{providerName}")
    @Produces(MediaType.TEXT_HTML)
    public Response initLogin(@RestPath String providerName) {
        log.debug("Init login {}.", providerName);
        // todo check if user is logged in already - abort
        SecurityService.Session session = securityService.createSession();
        SecurityService.OidcProvider provider = securityService.getOidcProvider(providerName);
        String state = provider.getName() + "_" + utils.generateRandomString(20);
        session.setParameter(STATE_ATTR, new LoginState(provider, state));
        log.debug("Session: {}", session);
        return Response.status(Response.Status.FOUND)
                .location(URI.create(provider.getAuthorizationEndpoint(state)))
                .cookie(new NewCookie(SESSION_ID_COOKIE, session.getSessionId(), "/", null, null, NewCookie.DEFAULT_MAX_AGE, false))
                .build();
    }

    /*
    RestResponse
     */
    @GET
    @Path("callback")
    @Produces(MediaType.TEXT_HTML)
    public Uni<Response> callback(UriInfo uriInfo, @RestCookie(SESSION_ID_COOKIE) String sessionId) {
        log.info("Callback: {}. SessionId: {}", uriInfo.getRequestUri(), sessionId);
        /* error_reason, error, error_description */
        String error = uriInfo.getQueryParameters().getFirst("error");
        if (error != null) {
            String errorReason = uriInfo.getQueryParameters().getFirst("error_reason");
            String errorDescription = uriInfo.getQueryParameters().getFirst("error_description");
            return Uni.createFrom().item(
                    Response
                            .status(Response.Status.BAD_REQUEST)
                            .entity(String.format("<!DOCTYPE html><html><body>" +
                            "<div>Error: %s</div>" +
                            "<div>Reason: %s</div>" +
                            "<div>Description: %s</div>" +
                            "</body></html>", error, errorReason, errorDescription))
                            .build()
            );
        }

        if (sessionId == null) {
            return Uni.createFrom().item(
                    Response
                            .status(Response.Status.BAD_REQUEST)
                            .entity("<!DOCTYPE html><html><body>" +
                                    "<div>Session cookie is not set</div>" +
                                    "</body></html>")
                            .build()
            );
        }

        String code = uriInfo.getQueryParameters().getFirst("code");
        String callbackState = uriInfo.getQueryParameters().getFirst("state");

        LoginState loginState = Optional.ofNullable(securityService.getSession(sessionId))
                .map(s -> s.<LoginState>getParameter(STATE_ATTR)).orElse(null);
        if (loginState == null || !callbackState.equals(loginState.getState())) {
            return Uni.createFrom().item(
                    Response
                            .status(Response.Status.BAD_REQUEST)
                            .entity("<!DOCTYPE html><html><body>" +
                                    "<div>Received invalid state</div>" +
                                    "</body></html>")
                            .build()
            );
        }
        securityService.removeSession(sessionId);

        SecurityService.OidcProvider oidcProvider = loginState.getProvider();
        return client.getAbs(oidcProvider.getTokenEndpoint())
                .setQueryParam("client_id", oidcProvider.getClientId())
                .setQueryParam("redirect_uri", oidcProvider.getRedirectUri())
                .setQueryParam("client_secret", oidcProvider.getSecret())
                .setQueryParam("code", code)
                        .send()
                .onItem().transform(HttpResponse::bodyAsJsonObject)
                .map(tokenJson -> {
                    log.debug("Token received: {}", tokenJson);
                    return tokenJson.getString("access_token");
                })
                /* profile_pic -> This call requires a Page access token.*/
                .onItem().transformToUni(token -> client.getAbs("https://graph.facebook.com/me?fields=id,email,gender,first_name,last_name,picture,birthday,link,location")
                        .bearerTokenAuthentication(token)
                        .send())
                .onItem().transform(HttpResponse::bodyAsJsonObject)
                .onItem().transformToUni(userJson -> {
                    log.debug("Facebook user response received: {}", userJson);

                    String userProviderId = userJson.getString("id");
                    String email = userJson.getString("email");

                    return dbUser.findById(oidcProvider.getName(), userProviderId, email, null)
                            .onItem().transform(searchResult -> Tuple.of(searchResult, userJson));
                })
                .onItem().transformToUni(info -> {
                    DbUser.UserSearchResult searchResult = info.get(DbUser.UserSearchResult.class, 0);
                    JsonObject userJson = info.get(JsonObject.class, 1);
                    SecurityService.Session newSession = securityService.createSession();
                    if (searchResult != null && searchResult.getType() == DbUser.UserSearchType.socialNetwork) {
                        // Login by social network id
                        newSession.setUserId(searchResult.getUserId());
                        return Uni.createFrom().item(newSession);
                    }

                    String id = userJson.getString("id");
                    String email = userJson.getString("email");
                    String link = userJson.getString("link");

                    Uni<Integer> userIdUni;
                    if (searchResult != null) {
                        // Add social network info
                        userIdUni = dbUser.addSocialNetwork(searchResult.getUserId(), oidcProvider.getName(), id, link, email, null)
                                .onItem().transform(u -> searchResult.getUserId());
                    } else {
                        // Create user id
                        String firstName = userJson.getString("first_name");
                        String lastName = userJson.getString("last_name");
                        userIdUni = dbUser.addUser(new DbUser.EntityUser()
                                        .setFirstName(firstName)
                                        .setLastName(lastName)
                                        .setType(DbUser.UserType.guest)
                                ).onItem().transformToUni(userId ->
                                        dbUser.addEmail(userId, email)
                                                .onItem().transform(u -> userId)
                                );
                        newSession.setParameter(NEW_USER_ATTR, true);
                    }

                    userIdUni = userIdUni.onItem().invoke(newSession::setUserId);

                    String imageUrl = Optional.ofNullable(userJson.getJsonObject("picture"))
                            .map(p -> p.getJsonObject("data"))
                            .map(d -> d.getString("url"))
                            .orElse(null);
                    // Load image if present and return new session
                    return userIdUni.onItem().transformToUni(userId -> {
                                if (Utils.isEmpty(imageUrl)) {
                                    return Uni.createFrom().item(newSession);
                                }
                                return client.getAbs(imageUrl).send()
                                        .onItem().transformToUni(image -> {
                                            String contentType = image.getHeader(HttpHeaders.CONTENT_TYPE);
                                            return dbUser.addImage(userId, image.body(), contentType)
                                                    .onItem().transformToUni(imageId -> dbUser.setMainImage(userId, imageId, true));
                                        })
                                        .onItem().transform(u -> newSession);
                            });
                }).onItem().transform(session -> {
                    Boolean newUser = session.removeParameter(NEW_USER_ATTR);

                    // return "<script>onLoginCompleted();</script>";
                    // probably temporary solution to avoid cross browser issue
                    return Response.status(Response.Status.FOUND)
//                            .location(URI.create("http://localhost:8081/callback/login-ok.html?session_id=<session_id>&user_type=<user_type>"
                            .location(URI.create("http://localhost:8081/login-ok?session_id=<session_id>&user_type=<user_type>"
                                    .replaceAll("<session_id>", session.getSessionId())
                                    .replaceAll("<user_type>", Boolean.TRUE.equals(newUser) ? "new" : "existing")
                            ))
                            .cookie(new NewCookie(SESSION_ID_COOKIE,  null, "/", null, null, 0, false, false))
                            .build();
                });
    }

    @GET
    @Path("user")
    @Produces(MediaType.APPLICATION_JSON)
    @Authenticated
    public Uni<DbUser.EntityUser> user() {
        int userId = securityService.getUserId();
        return dbUser.getUser(userId);
    }

    @GET
    @Path("userFull")
    @Produces(MediaType.APPLICATION_JSON)
    @Authenticated
    public Uni<DbUser.EntityUser> userFull() {
        int userId = securityService.getUserId();
        return dbUser.getUserFull(userId);
    }

    private static class LoginState {
        private SecurityService.OidcProvider provider;
        private String state;

        public LoginState(SecurityService.OidcProvider provider, String state) {
            this.state = state;
            this.provider = provider;
        }

        public SecurityService.OidcProvider getProvider() {
            return provider;
        }

        public String getState() {
            return state;
        }
    }
}
