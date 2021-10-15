package org.mpashka.totemftc.api;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;

/**
 * https://vk.com/editapp?id=7929993
 *
 * https://vk.com/dev/authcode_flow_user
 * https://vk.com/dev/permissions
 *
 */
@ApplicationScoped
public class AuthProviderOidcVk extends AuthProviderOidc {

    private static final Logger log = LoggerFactory.getLogger(AuthProviderOidcVk.class);

    @ConfigProperty(name = "oidc.provider.vk.version", defaultValue = "5.131")
    String vkVersion;

    public AuthProviderOidcVk() {
        super("vk",
                "email",
                "https://oauth.vk.com/authorize?client_id=<client_id>&redirect_uri=<redirect_uri>&scope=<scope>&response_type=code",
                "https://api.instagram.com/oauth/access_token");
    }

    /*
    {"access_token":"...","expires_in":86333,"user_id":91831849,"email":"m_pashka@mail.ru"}
     */
    void processTokenResponse(WebResourceLogin.LoginState loginState, JsonObject tokenJson) {
        loginState.setAuthUserInfo(new UserInfo(
                tokenJson.getString("user_id"), null, tokenJson.getString("email"), null, null, null, null, null
        ));
    }


    /**
     * https://vk.com/dev/access_token
     * https://vk.com/dev/api_requests
     * https://vk.com/dev/methods
     * https://vk.com/dev/users.get
     * https://vk.com/dev/account.getProfileInfo
     * https://vk.com/dev/objects/user
     *
     * connections:
     *      "skype", "facebook", "facebook_name", "twitter", "livejournal", "instagram"
     *
     * Note: account.getProfileInfo requires advanced privileges that can be granted only to
     *       standalone app
     */
    @Override
    public Uni<UserInfo> readUserInfo(WebResourceLogin.LoginState loginState) {
        String fields = "id,first_name,last_name,photo_max_orig,connections,contacts,domain";
        return getWebClient()
                .getAbs("https://api.vk.com/method/users.get?fields=<fields>&name_case=nom&v=<vk_version>&access_token=<access_token>"
                        .replace("<access_token>", loginState.getToken())
                        .replace("<vk_version>", vkVersion)
                        .replace("<fields>", fields)
                ).send()
                .onItem().transform(HttpResponse::bodyAsJsonObject)
                .onItem().transform(userJson -> {
                    log.debug("VK user response received: {}", userJson);
                    String email = loginState.getAuthUserInfo().getEmail();
                    String domain = userJson.getString("domain");
                    boolean isNick = domain.matches("id\\d+");

                    return new UserInfo(userJson.getString("id"),
                            "https://vk.com/<domain>".replace("<domain>", domain),
                            email,
                            null,
                            userJson.getString("first_name"),
                            userJson.getString("last_name"),
                            isNick ? domain : null,
                            userJson.getString("photo_max_orig"));
                });
    }
}
