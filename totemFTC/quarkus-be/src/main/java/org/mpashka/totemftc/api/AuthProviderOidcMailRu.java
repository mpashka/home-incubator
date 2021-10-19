package org.mpashka.totemftc.api;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;

/**
 * https://o2.mail.ru/app/
 * https://oauth.mail.ru/docs
 */
@ApplicationScoped
public class AuthProviderOidcMailRu extends AuthProviderOidc {

    private static final Logger log = LoggerFactory.getLogger(AuthProviderOidcMailRu.class);

    public AuthProviderOidcMailRu() {
        super("mailru",
                "openid+userinfo+email+profile+offline_access",
                "https://oauth.mail.ru/login?client_id=<client_id>&redirect_uri=<redirect_uri>&scope=<scope>&state=<state>&response_type=code&nonce=<nonce>",
                "https://oauth.mail.ru/token");
    }

    /**
     * https://oauth.mail.ru/docs#48-userinformationfromthetoken
     */
    @Override
    public Uni<UserInfo> readUserInfo(WebResourceLogin.LoginState loginState) {
        return getWebClient()
                .getAbs("https://oauth.mail.ru/userinfo?userinfo&access_token=<access_token>"
                        .replace("<access_token>", loginState.getToken())
                ).send()
                .onItem().transform(HttpResponse::bodyAsJsonObject)
                .onItem().transform(userJson -> {
                    log.debug("Mail.ru user response received: {}", userJson);

                    return new UserInfo(getName(),
                            userJson.getString("id"),
                            null,
                            userJson.getString("email"),
                            null,
                            userJson.getString("first_name"),
                            userJson.getString("last_name"),
                            userJson.getString("nickname"),
                            userJson.getString("image"));
                });
    }
}
