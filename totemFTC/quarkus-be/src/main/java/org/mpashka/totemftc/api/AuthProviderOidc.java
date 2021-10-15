package org.mpashka.totemftc.api;

import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriInfo;

/**
 * https://openid.net/specs/openid-connect-core-1_0.html#StandardClaims
 */
public abstract class AuthProviderOidc extends AuthProvider {

    private static final Logger log = LoggerFactory.getLogger(AuthProviderOidc.class);

    private String clientId;
    private String secret;
    private String tokenEndpoint;

    public AuthProviderOidc(String name, String scope, String authorizationEndpoint, String tokenEndpoint) {
        super(name);
        this.clientId = ConfigProvider.getConfig().getValue("oidc.provider.<provider>.clientId".replace("<provider>", name), String.class);
        this.secret = ConfigProvider.getConfig().getValue("oidc.provider.<provider>.secret".replace("<provider>", name), String.class);
        setAuthorizationEndpoint(authorizationEndpoint
                .replace("<client_id>", clientId)
                .replace("<redirect_uri>", getRedirectUri())
                .replace("<scope>", scope)
                .replace("<backend_url>", getBackendUrl()));
        this.tokenEndpoint = tokenEndpoint;
    }

    public String getSecret() {
        return secret;
    }

    @Override
    public Uni<WebResourceLogin.LoginState> processCallback(UriInfo uriInfo, WebResourceLogin.LoginState loginState) {
        String code = uriInfo.getQueryParameters().getFirst("code");

        return getWebClient().requestAbs(HttpMethod.POST, tokenEndpoint)
                .putHeader("Accept", "application/json")
                .setQueryParam("client_id", clientId)
                .setQueryParam("client_secret", secret)
                .setQueryParam("redirect_uri", getRedirectUri())
                .setQueryParam("code", code)
                .setQueryParam("grant_type", "authorization_code")
                .send()
                .onItem().transform(HttpResponse::bodyAsJsonObject)
                .map(tokenJson -> {
                    log.debug("Token received: {}", tokenJson);
                    // expires_in,id_token,scope,token_type,refresh_token
                    String accessToken = tokenJson.getString("access_token");
                    if (accessToken == null || accessToken.isEmpty()) {
                        throw new RuntimeException("Invalid response " + tokenJson);
                    }
                    loginState.setToken(accessToken);
                    processTokenResponse(loginState, tokenJson);
                    return loginState;
                });
    }

    void processTokenResponse(WebResourceLogin.LoginState loginState, JsonObject tokenJson) {}
}
