package org.mpashka.totemftc.api;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.UriInfo;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * https://ok.ru/vitrine/myuploaded
 * https://apiok.ru/ext/oauth/server
 * https://apiok.ru/ext/oauth/permissions
 */
@ApplicationScoped
public class AuthProviderOkRu extends AuthProviderOidc {

    private static final Logger log = LoggerFactory.getLogger(AuthProviderOkRu.class);

    @ConfigProperty(name = "oidc.provider.okru.publicKey")
    String applicationPublicKey;

    private MessageDigest md5;
    private String secretSignKey;

    public AuthProviderOkRu() {
        super("okru",
                "VALUABLE_ACCESS;GET_EMAIL;LONG_ACCESS_TOKEN",
                "https://connect.ok.ru/oauth/authorize?client_id=<client_id>&redirect_uri=<redirect_uri>&scope=<scope>&state=<state>&response_type=code&nonce=<nonce>",
                "https://api.ok.ru/oauth/token.do");
    }

    @PostConstruct
    void init() throws NoSuchAlgorithmException {
        md5 = MessageDigest.getInstance("MD5");
    }

    /**
     * ok.ru doesn't send state back, so ignore
     */
    @Override
    void checkState(UriInfo uriInfo, WebResourceLogin.LoginState loginState) {
    }

    @Override
    void processTokenResponse(WebResourceLogin.LoginState loginState, JsonObject tokenJson) {
        String accessToken = loginState.getToken();
        md5.reset();
        md5.update((accessToken + getSecret()).getBytes());
        secretSignKey = Utils.bytesToHex(md5.digest(), false);
    }

    /**
     * https://apiok.ru/dev/methods/
     * https://apiok.ru/dev/methods/rest/users/users.getCurrentUser
     */
    @Override
    public Uni<UserInfo> readUserInfo(WebResourceLogin.LoginState loginState) {
        String fields = "UID,EMAIL,FIRST_NAME,LAST_NAME,GENDER,PIC_BASE,URL_PROFILE,PIC1024X768,BIRTHDAY";
        String sigString = "application_key=<application_public_key>fields=<fields>method=users.getCurrentUser<secret_sign_key>"
                .replace("<application_public_key>", applicationPublicKey)
                .replace("<fields>", fields)
                .replace("<secret_sign_key>", secretSignKey);
        md5.reset();
        md5.update(sigString.getBytes());
        String sig = Utils.bytesToHex(md5.digest(), false);

        return getWebClient()
                .getAbs("https://api.ok.ru/fb.do?method=users.getCurrentUser&application_key=<application_public_key>&fields=<fields>&sig=<sig>&access_token=<access_token>"
                        .replace("<application_public_key>", applicationPublicKey)
                        .replace("<access_token>", loginState.getToken())
                        .replace("<fields>", fields)
                        .replace("<sig>", sig)
                ).send()
                .onItem().transform(HttpResponse::bodyAsJsonObject)
                .onItem().transform(userJson -> {
                    log.debug("Ok.ru user response received: {}", userJson);

                    return new UserInfo(getName(),
                            userJson.getString("uid"),
                            userJson.getString("url_profile"),
                            userJson.getString("email"),
                            null,
                            userJson.getString("first_name"),
                            userJson.getString("last_name"),
                            null,
                            userJson.getString("image"));
                });
    }
}
