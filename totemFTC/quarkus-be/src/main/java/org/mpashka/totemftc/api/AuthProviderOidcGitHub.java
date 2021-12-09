package org.mpashka.totemftc.api;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;

/**
 * https://github.com/settings/developers
 * https://github.com/settings/applications/1691748
 *
 * https://docs.github.com/en/developers/apps/building-oauth-apps/authorizing-oauth-apps
 * https://docs.github.com/en/developers/apps/building-oauth-apps/scopes-for-oauth-apps
 */
@ApplicationScoped
public class AuthProviderOidcGitHub extends AuthProviderOidc {

    private static final Logger log = LoggerFactory.getLogger(AuthProviderOidcGitHub.class);

    public AuthProviderOidcGitHub() {
        super("github",
                "read:user+user:email",
                "https://github.com/login/oauth/authorize?client_id=<client_id>&redirect_uri=<redirect_uri>&state=<state>&scope=<scope>&state=<state>&response_type=code&nonce=<nonce>",
                "https://github.com/login/oauth/access_token");
    }

    /**
     * https://docs.github.com/en/rest/reference/users
     *
     */
    @Override
    public Uni<UserInfo> readUserInfo(WebResourceLogin.LoginState loginState) {
        /* profile_pic -> This call requires a Page access token.*/
        return getWebClient()
                .getAbs("https://api.github.com/user")
                .bearerTokenAuthentication(loginState.getToken())
                .send()
                .onItem().transform(HttpResponse::bodyAsJsonObject)
                .onItem().transform(userJson -> {
                    log.debug("GitHub user response received: {}", userJson);

                    String name = userJson.getString("name");
                    String[] names = name != null ? name.split("\\s+") : new String[0];

                    return new UserInfo(getName(),
                            userJson.getString("id"),
                            userJson.getString("html_url"),
                            userJson.getString("email"),
                            null,
                            name,
                            names.length > 0 ? names[0] : null,
                            names.length > 1 ? names[1] : null,
                            userJson.getString("login"),
                            userJson.getString("avatar_url"));
                });
    }
}
