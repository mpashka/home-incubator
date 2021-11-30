package org.mpashka.totemftc.api;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.util.Optional;

/**
 * https://oauth.yandex.ru/
 * https://yandex.ru/dev/oauth/
 * https://yandex.ru/dev/oauth/doc/dg/reference/auto-code-client.html
 * https://oauth.yandex.ru/client/${client_id}/info
 */
@ApplicationScoped
public class AuthProviderOidcYandex extends AuthProviderOidc {

    private static final Logger log = LoggerFactory.getLogger(AuthProviderOidcYandex.class);

    /**
     * todo see https://yandex.ru/dev/oauth/doc/dg/reference/auto-code-client.html
     * add device_id, device_name, force_confirm=yes
     */
    public AuthProviderOidcYandex() {
        super("yandex",
                "login:birthday+login:email+login:info+login:avatar",
                "https://oauth.yandex.ru/authorize?force_confirm=yes&client_id=<client_id>&redirect_uri=<redirect_uri>&state=<state>&response_type=code&scope=<scope>&nonce=<nonce>",
                "https://oauth.yandex.ru/token");
    }

    /**
     * https://yandex.ru/dev/id/doc/dg/reference/request.html
     */
    @Override
    public Uni<UserInfo> readUserInfo(WebResourceLogin.LoginState loginState) {
        return getWebClient()
                .getAbs("https://login.yandex.ru/info?format=json")
                .bearerTokenAuthentication(loginState.getToken())
                .send()
                .onItem().transform(HttpResponse::bodyAsJsonObject)
                .onItem().transform(userJson -> {
                    log.debug("Yandex user response received: {}", userJson);
                    boolean emptyAvatar = userJson.getBoolean("is_avatar_empty");
                    String imageUrl = null;
                    if (!emptyAvatar) {
                        String avatarId = userJson.getString("default_avatar_id");
                        String size = "islands-200";
                        imageUrl = "https://avatars.yandex.net/get-yapic/<avatar_id>/<size>"
                                .replace("<avatar_id>", avatarId)
                                .replace("<size>", size);
                    }

//                    userJson.getString("login")
//                    userJson.getString("old_social_login")
//                    userJson.getString("birthday")
//                    userJson.getString("sex")
// todo email is string array
//                    userJson.getJsonArray("email"),

                    return new UserInfo(getName(),
                            userJson.getString("id"),
                            null,
                            userJson.getString("default_email"),
                            null,
                            userJson.getString("display_name"),
                            userJson.getString("first_name"),
                            userJson.getString("last_name"),
                            userJson.getString("display_name"),
                            imageUrl);
                });
    }
}
