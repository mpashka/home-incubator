package org.mpashka.totemftc.api;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.util.Optional;

/**
 * https://developers.google.com/oauthplayground/
 *
 * https://console.cloud.google.com/apis/dashboard?pli=1&project=totemftc
 *
 * https://developers.google.com/identity/protocols/oauth2
 * https://developers.google.com/identity/protocols/oauth2/openid-connect
 * https://developers.google.com/identity/protocols/oauth2/scopes
 * https://accounts.google.com/.well-known/openid-configuration
 *
 * https://developers.google.com/identity/protocols/oauth2/scopes:
 *      https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email email openid profile
 *
 * todo <login_hint> endpoint param
 */
@ApplicationScoped
public class AuthProviderOidcGoogle extends AuthProviderOidc {

    private static final Logger log = LoggerFactory.getLogger(AuthProviderOidcGoogle.class);

    public AuthProviderOidcGoogle() {
        super("google",
                "openid+email+profile",
                "https://accounts.google.com/o/oauth2/v2/auth?client_id=<client_id>&redirect_uri=<redirect_uri>&state=<state>&response_type=code&scope=<scope>&nonce=<nonce>",
                "https://oauth2.googleapis.com/token");
    }

    /**
     * https://any-api.com/googleapis_com/oauth2/docs/userinfo/oauth2_userinfo_get
     *
     */
    @Override
    public Uni<UserInfo> readUserInfo(WebResourceLogin.LoginState loginState) {
        /* profile_pic -> This call requires a Page access token.*/
        return getWebClient()
                .getAbs("https://www.googleapis.com/oauth2/v2/userinfo")
                .bearerTokenAuthentication(loginState.getToken())
                .send()
                .onItem().transform(HttpResponse::bodyAsJsonObject)
                .onItem().transform(userJson -> {
                    log.debug("Google user response received: {}", userJson);

// todo                   userJson.getBoolean("verified_email"),
                    return new UserInfo(getName(),
                            userJson.getString("id"),
                            userJson.getString("link"),
                            userJson.getString("email"),
                            null,
                            userJson.getString("given_name"),
                            userJson.getString("family_name"),
                            null,
                            userJson.getString("picture"));
                });
    }
}
