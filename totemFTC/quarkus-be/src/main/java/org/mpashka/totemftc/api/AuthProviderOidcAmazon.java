package org.mpashka.totemftc.api;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * https://developer.amazon.com/docs/login-with-amazon/authorization-code-grant.html#server-apps
 * https://developer.amazon.com/settings/console/securityprofile/web-settings/update.html
 *
 * todo [5] code_challenge=<nonce>&code_challenge_method=S256
 * todo [5] scope: postal_code
 */
@ApplicationScoped
public class AuthProviderOidcAmazon extends AuthProviderOidc {

    private static final Logger log = LoggerFactory.getLogger(AuthProviderOidcAmazon.class);

    public AuthProviderOidcAmazon() {
        super("amazon",
                "profile",
                "https://www.amazon.com/ap/oa?client_id=<client_id>&redirect_uri=<redirect_uri>&scope=<scope>&state=<state>&response_type=code&nonce=<nonce>",
                "https://api.amazon.co.uk/auth/o2/token");
    }

    /**
     * https://developer.amazon.com/docs/login-with-amazon/obtain-customer-profile.html#call-profile-endpoint
     */
    @Override
    public Uni<UserInfo> readUserInfo(WebResourceLogin.LoginState loginState) {
        return getWebClient()
                .getAbs("https://api.amazon.com/user/profile")
                .bearerTokenAuthentication(loginState.getToken())
                .send()
                .onItem().transform(HttpResponse::bodyAsJsonObject)
                .onItem().transform(userJson -> {
                    log.debug("Amazon user response received: {}", userJson);

                    return new UserInfo(getName(),
                            userJson.getString("user_id"),
                            null,
                            userJson.getString("email"),
                            null,
                            userJson.getString("name"),
                            null,
                            null,
                            null,
                            null);
                });
    }
}
