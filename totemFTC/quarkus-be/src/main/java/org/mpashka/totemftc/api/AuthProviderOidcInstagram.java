package org.mpashka.totemftc.api;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;

/**
 * https://developers.facebook.com/docs/instagram-basic-display-api/
 * https://developers.facebook.com/docs/instagram-basic-display-api/getting-started
 */
@ApplicationScoped
public class AuthProviderOidcInstagram extends AuthProviderOidc {

    private static final Logger log = LoggerFactory.getLogger(AuthProviderOidcInstagram.class);

    public AuthProviderOidcInstagram() {
        super("instagram",
                "openid,user_profile",
                "https://api.instagram.com/oauth/authorize?client_id=<client_id>&redirect_uri=<redirect_uri>&state=<state>&scope=<scope>&response_type=code&nonce=<nonce>",
                "https://api.instagram.com/oauth/access_token");
    }

    /**
     * https://developers.facebook.com/docs/instagram-basic-display-api/reference/me
     * https://developers.facebook.com/docs/instagram-basic-display-api/reference/user
     *
     * Get image howto: https://stackoverflow.com/questions/50086945/how-to-get-instagram-profile-picture-via-api
     * - https://www.instagram.com/USERNAME/?__a=1
     *      +graphql.user.*
     *          +facebook profile id - fbid
     *          +profile_pic_url
     *          +profile_pic_url_hd
     *          +full_name
     *          +business_email
     *          +business_phone_number
     *
     * - https://i.instagram.com/api/v1/users/USER_ID/info/ (just image, requires specific user agent)
     */
    @Override
    public Uni<UserInfo> readUserInfo(WebResourceLogin.LoginState loginState) {
        /* profile_pic -> This call requires a Page access token.*/
        return getWebClient()
                .getAbs("https://graph.instagram.com/me?fields=id,username&access_token=<access_token>"
                        .replace("<access_token>", loginState.getToken()))
                .send()
                .onItem().transform(HttpResponse::bodyAsJsonObject)
                .onItem().transformToUni(userShortJson -> {
                    log.debug("Instagram user response received: {}", userShortJson);
                    String username = userShortJson.getString("username");

                    return getWebClient().getAbs("https://www.instagram.com/<username>/?__a=1"
                                    .replace("<username>", username))
                            .send()
                            .onItem().transform(HttpResponse::bodyAsJsonObject)
                            .onItem().transform(userInfoJson -> {
                                JsonObject userJson = userInfoJson.getJsonObject("graphql").getJsonObject("user");
                                String name = userJson.getString("full_name");
                                String[] names = name != null ? name.split("\\s+") : new String[0];
                                return new UserInfo(getName(),
                                        userJson.getString("id"),
                                        "https://www.instagram.com/<username>/".replace("<username>", username),
                                        null,
                                        null,
                                        userJson.getString("username"),
                                        names.length > 0 ? names[0] : null,
                                        names.length > 1 ? names[1] : null,
                                        userJson.getString("username"),
                                        userJson.getString("profile_pic_url_hd"));
                            });
                });
    }
}
