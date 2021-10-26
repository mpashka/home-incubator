package org.mpashka.totemftc.api;

import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.MultiMap;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.ConfigValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriInfo;

/**
 * https://openid.net/specs/openid-connect-core-1_0.html#StandardClaims
 */
public abstract class AuthProviderOidc extends AuthProvider {

    private static final Logger log = LoggerFactory.getLogger(AuthProviderOidc.class);

    private String backendUrl;
    private String clientId;
    private String secret;
    private String tokenEndpoint;
    private String redirectUri;
    private String authorizationEndpoint;

    public AuthProviderOidc(String name, String scope, String authorizationEndpoint, String tokenEndpoint) {
        super(name);
        this.clientId = ConfigProvider.getConfig().getValue("oidc.provider.<provider>.clientId".replace("<provider>", name), String.class);
        this.secret = ConfigProvider.getConfig().getValue("oidc.provider.<provider>.secret".replace("<provider>", name), String.class);
        this.backendUrl = ConfigProvider.getConfig().getValue("app.url.backend", String.class);
        this.redirectUri = "<backend_url>/api/login/callback/<provider>"
                .replace("<backend_url>", backendUrl)
                .replace("<provider>", name);

        this.authorizationEndpoint = authorizationEndpoint
                .replace("<client_id>", clientId)
                .replace("<redirect_uri>", redirectUri)
                .replace("<scope>", scope)
                .replace("<backend_url>", backendUrl);
        this.tokenEndpoint = tokenEndpoint;
    }

    String getSecret() {
        return secret;
    }

    @Override
    public String getAuthorizationEndpoint() {
        return authorizationEndpoint;
    }

    @Override
    public Uni<WebResourceLogin.LoginState> processCallback(UriInfo uriInfo, WebResourceLogin.LoginState loginState, String client) {
        String code = uriInfo.getQueryParameters().getFirst("code");
        ConfigValue redirectUriConfig = ConfigProvider.getConfig().getConfigValue("oidc.client.<clientId>.redirectUri".replace("<clientId>", client));
        String redirectUri = redirectUriConfig.getValue() == null
                ? this.redirectUri
                : redirectUriConfig.getValue().replace("<provider>", getName());

        MultiMap form = MultiMap.caseInsensitiveMultiMap()
                .set("client_id", clientId)
                .set("client_secret", secret)
                .set("redirect_uri", redirectUri)
                .set("code", code)
                .set("grant_type", "authorization_code");
        
        return getWebClient().requestAbs(HttpMethod.POST, tokenEndpoint)
                .putHeader("Accept", "application/json")
                .sendForm(form)
                .onItem().transform(r -> {
                    try {
                        return r.bodyAsJsonObject();
                    } catch (Exception e) {
                        throw new RuntimeException("Can't parse token json\n" + r.bodyAsString(), e);
                    }
                }).map(tokenJson -> {
                    log.debug("Auth token response received: {}", tokenJson);
                    // expires_in,id_token,scope,token_type,refresh_token
                    String accessToken = tokenJson.getString("access_token");
                    if (accessToken == null || accessToken.isEmpty()) {
                        throw new RuntimeException("Invalid token response " + tokenJson);
                    }
                    loginState.setToken(accessToken);
                    processTokenResponse(loginState, tokenJson);
                    return loginState;
                });
    }

    void processTokenResponse(WebResourceLogin.LoginState loginState, JsonObject tokenJson) {}
}
