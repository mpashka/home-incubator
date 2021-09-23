package org.mpashka.totemftc.api;

import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.jboss.resteasy.reactive.RestCookie;
import org.jboss.resteasy.reactive.RestPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Path("/login")
public class WebResourceLogin {

    private static final Logger log = LoggerFactory.getLogger(WebResourceLogin.class);

    private static final String STATE_ATTR = "login:state";
    private static final String SESSION_ID_COOKIE = "sessionId";

    private final WebClient client;

    @Inject
    SecurityService securityService;

    @Inject
    Utils utils;

    @Inject
    DBUser dbUser;

    @Inject
    ManagedExecutor exec;

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

        AtomicReference<JsonObject> userInfoJson = new AtomicReference<>();
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
                .onItem().transformToUni(token -> client.getAbs("https://graph.facebook.com/me?fields=id,email,gender,first_name,last_name,picture,birthday,link,location")
                        .bearerTokenAuthentication(token)
                        .send())
                .onItem().transform(HttpResponse::bodyAsJsonObject)
                .onItem().transformToUni(userJson -> {
                    log.debug("Facebook user response received: {}", userJson);
                    userInfoJson.set(userJson);

                    String userProviderId = userJson.getString("id");
                    String email = userJson.getString("email");

                    return dbUser.findById(oidcProvider.getName(), userProviderId, email, null);
                })
                .onItem().transformToUni(userId -> {
                    SecurityService.Session newSession = securityService.createSession();
                    if (userId != null && userId.getType() == DBUser.UserSearchType.socialNetwork) {
                        // Login by social network id
                        newSession.setUserId(userId.getUserId());
                        return Uni.createFrom().item(userId);
                    }

                    JsonObject userJson = userInfoJson.get();
                    String id = userJson.getString("id");
                    String email = userJson.getString("email");
                    String imageUrl = Optional.ofNullable(userJson.getJsonObject("picture"))
                            .map(p -> p.getJsonObject("data"))
                            .map(d -> d.getString("url"))
                            .orElse(null);


                    Uni<HttpResponse<Buffer>> image = Utils.notEmpty(imageUrl)
                    ? client.getAbs(imageUrl).send()
                            : Uni.createFrom().item(null);
                    if (userId != null) {
                        // Add social network info
                        return dbUser.addSocialNetwork(userId.getUserId(), oidcProvider.getName(), id, email, null)
                                .onItem().transform(u -> userId);
                    } else {
                        // Create user id
                        return dbUser.
                    }




                    String userName = userJson.getString("name");

                    SecurityService.UserInfo userInfo = new SecurityService.UserInfo(id, userName, imageUrl);
                    securityService.setUserInfo(userInfo);
                    if (imageUrl != null) {
                        exec.runAsync(() -> );
                    }

                    //                    return "<script>onLoginCompleted();</script>";
                    // probably temporary solution to avoid cross browser issue
                    return Response.status(Response.Status.FOUND)
                            .location(URI.create("http://localhost:8081/callback/login-ok.html?session_id=<session_id>".replaceAll("<session_id>", newSession.getSessionId())))
                            .cookie(new NewCookie(SESSION_ID_COOKIE,  null, null, null, null, 0, false, false))
                            .build();
                });

        /*
        Facebook user response received: {"id":"4493174020705731","email":"m_pashka@mail.ru","first_name":"Павел","last_name":"Мухатаев","name":"Павел Мухатаев",
        "picture":{"data":{"height":50,"is_silhouette":false,"url":"https://platform-lookaside.fbsbx.com/platform/profilepic/?asid=4493174020705731&height=50&width=50&ext=1634393350&hash=AeR_Nf_Xl2kF7_GVmdU","width":50}},"short_name":"Павел","gender":"male","name_format":"{first} {last}"}
         */
    }

    @GET
    @Path("userInfo")
    @Produces(MediaType.APPLICATION_JSON)
    @Authenticated
    @Blocking
    public Uni<SecurityService.UserInfo> userInfo() {
        SecurityService.UserInfo userInfo = securityService.getUserInfo();
        return Uni.createFrom().item(userInfo);
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
