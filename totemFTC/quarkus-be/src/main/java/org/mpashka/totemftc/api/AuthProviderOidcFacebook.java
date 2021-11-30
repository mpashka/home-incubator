package org.mpashka.totemftc.api;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Optional;

/**
 * https://developers.facebook.com/apps/220460853218319/dashboard/
 * https://developers.facebook.com/apps/558989508631164/dashboard/ - test app
 * https://developers.facebook.com/apps/558989508631164/settings/basic/ - client id + secret
 *
 * https://developers.facebook.com/tools/explorer/ - graph api explorer
 *
 * https://developers.facebook.com/docs/facebook-login/manually-build-a-login-flow - oidc description
 *
 */
@ApplicationScoped
public class AuthProviderOidcFacebook extends AuthProviderOidc {

    private static final Logger log = LoggerFactory.getLogger(AuthProviderOidcFacebook.class);

    public AuthProviderOidcFacebook() {
        super("facebook",
                "openid+email+public_profile+user_gender+user_link+user_birthday+user_location",
                "https://www.facebook.com/dialog/oauth?client_id=<client_id>&redirect_uri=<redirect_uri>&state=<state>&response_type=code&scope=<scope>&nonce=<nonce>",
                "https://graph.facebook.com/oauth/access_token");
    }

    /**
     * see https://developers.facebook.com/docs/graph-api/reference/user/#default-public-profile-fields
     * +short_name
     */
    @Override
    public Uni<UserInfo> readUserInfo(WebResourceLogin.LoginState loginState) {
        /* profile_pic -> This call requires a Page access token.*/
        return getWebClient()
                .getAbs("https://graph.facebook.com/me?fields=id,email,gender,first_name,last_name,name,picture,birthday,link,location")
                .bearerTokenAuthentication(loginState.getToken())
                .send()
                .onItem().transform(HttpResponse::bodyAsJsonObject)
                .onItem().transform(userJson -> {
                    log.debug("Facebook user response received: {}", userJson);

                    String imageUrl = Optional.ofNullable(userJson.getJsonObject("picture"))
                            .map(p -> p.getJsonObject("data"))
                            .map(d -> d.getString("url"))
                            .orElse(null);

                    return new UserInfo(getName(),
                            userJson.getString("id"),
                            userJson.getString("link"),
                            userJson.getString("email"),
                            null,
                            userJson.getString("name"),
                            userJson.getString("first_name"),
                            userJson.getString("last_name"),
                            null,
                            imageUrl);
                });
    }
}
