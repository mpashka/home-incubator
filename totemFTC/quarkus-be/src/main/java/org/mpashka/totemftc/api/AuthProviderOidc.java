package org.mpashka.totemftc.api;

import io.quarkus.runtime.configuration.ProfileManager;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.MultiMap;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.ConfigValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * https://openid.net/specs/openid-connect-core-1_0.html#StandardClaims
 */
public abstract class AuthProviderOidc extends AuthProvider {

    private static final Logger log = LoggerFactory.getLogger(AuthProviderOidc.class);

    private String tokenEndpoint;
    private String authorizationEndpoint;
    private String scope;

    public AuthProviderOidc(String name, String scope, String authorizationEndpoint, String tokenEndpoint) {
        super(name);
        this.scope = scope;
        this.authorizationEndpoint = authorizationEndpoint;
        this.tokenEndpoint = tokenEndpoint;
    }

    String getClientId(WebResourceLogin.ClientId clientId) {
        return findConfigValue(clientId,
                k -> k.isEmpty()
                ? "oidc.provider.<provider>.clientId".replace("<provider>", getName())
                : "oidc.provider.<provider>.<clientId>.clientId".replace("<provider>", getName()).replace("<clientId>", String.join("-", k)));
    }

    String getSecret(WebResourceLogin.ClientId clientId) {
        return findConfigValue(clientId,
                k -> k.isEmpty()
                ? "oidc.provider.<provider>.secret".replace("<provider>", getName())
                : "oidc.provider.<provider>.<clientId>.secret".replace("<provider>", getName()).replace("<clientId>", String.join("-", k)));
    }

    @Override
    public String getAuthorizationEndpoint(WebResourceLogin.ClientId clientId) {
        return authorizationEndpoint
                .replace("<client_id>", getClientId(clientId))
                .replace("<redirect_uri>", findRedirectUri(clientId))
                .replace("<scope>", scope);
    }

    @Override
    public Uni<WebResourceLogin.LoginState> processCallback(UriInfo uriInfo, WebResourceLogin.LoginState loginState, WebResourceLogin.ClientId clientId) {
        String code = uriInfo.getQueryParameters().getFirst("code");

        String redirectUri = findRedirectUri(clientId);

        MultiMap form = MultiMap.caseInsensitiveMultiMap()
                .set("client_id", getClientId(clientId))
                .set("client_secret", getSecret(clientId))
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
                        log.debug("Auth info. ClientId: {}. Form: {}", clientId, form);
                        throw new RuntimeException("Invalid token response " + tokenJson);
                    }
                    loginState.setToken(accessToken);
                    processTokenResponse(loginState, tokenJson, clientId);
                    return loginState;
                });
    }

    void processTokenResponse(WebResourceLogin.LoginState loginState, JsonObject tokenJson, WebResourceLogin.ClientId clientId) {}


    private String findRedirectUri(WebResourceLogin.ClientId clientId) {
        String configValue = findConfigValue(clientId, k -> k.isEmpty() ? "oidc.client.redirectUri" : ("oidc.client.redirectUri." + String.join("-", k)));
        if (configValue == null) {
            log.error("Was not able to find redirectUri for {}", clientId);
            return ConfigProvider.getConfig().getConfigValue("oidc.client.redirectUri").getValue();
        }
        return configValue.replace("<provider>", getName());
    }

    private String findConfigValue(WebResourceLogin.ClientId clientId, Function<List<String>, String> keyFunction) {
        String[] clientIdParts = clientId.getClientId().split("-");
        String[] idParts = new String[clientIdParts.length + 1];
        idParts[0] = ProfileManager.getActiveProfile();
        System.arraycopy(clientIdParts, 0, idParts, 1, clientIdParts.length);
        int mask = 1 << idParts.length;
        int maxKeyLength = -1;
        String value = null;
        while (--mask >= 0) {
            List<String> key = new ArrayList<>();
            for (int i = 0; i < idParts.length; i++) {
                boolean isSet = (mask & (1 << i)) != 0;
                if (isSet) {
                    key.add(idParts[i]);
                }
            }
            if (maxKeyLength >= key.size()) {
                continue;
            }
            ConfigValue redirectUriConfig = ConfigProvider.getConfig().getConfigValue(keyFunction.apply(key));
            if (redirectUriConfig.getValue() != null) {
                value = redirectUriConfig.getValue();
                maxKeyLength = key.size();
            }
        }
        return value;
    }
}
