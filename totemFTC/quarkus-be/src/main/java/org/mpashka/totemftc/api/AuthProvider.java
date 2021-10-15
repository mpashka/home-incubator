package org.mpashka.totemftc.api;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;
import org.eclipse.microprofile.config.ConfigProvider;

import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;

public abstract class AuthProvider {

    @Inject
    WebClient webClient;

    private String name;
    private String authorizationEndpoint;
    private String redirectUri;
    private String backendUrl;

    public AuthProvider(String name) {
        this.name = name;
        this.backendUrl = ConfigProvider.getConfig().getValue("app.url.backend", String.class);
        this.redirectUri = "<backend_url>/login/callback/<provider>"
                .replace("<backend_url>", backendUrl)
                .replace("<provider>", name);
    }

    public WebClient getWebClient() {
        return webClient;
    }

    public String getName() {
        return name;
    }

    public String getAuthorizationEndpoint() {
        return authorizationEndpoint;
    }

    public void setAuthorizationEndpoint(String authorizationEndpoint) {
        this.authorizationEndpoint = authorizationEndpoint;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public String getBackendUrl() {
        return backendUrl;
    }

    public abstract Uni<WebResourceLogin.LoginState> processCallback(UriInfo uriInfo, WebResourceLogin.LoginState loginState);

    public abstract Uni<UserInfo> readUserInfo(WebResourceLogin.LoginState loginState);

    static class UserInfo {
        /** Social network user id */
        private String id;
        private String link;
        private String email;
        private String phone;
        private String firstName;
        private String lastName;
        private String nickName;
        private String imageUrl;

        public UserInfo(String id, String link, String email, String phone, String firstName, String lastName, String nickName, String imageUrl) {
            this.id = id;
            this.link = link;
            this.email = email;
            this.phone = Utils.normalizePhone(phone);
            this.firstName = firstName;
            this.lastName = lastName;
            this.nickName = nickName;
            this.imageUrl = imageUrl;
        }

        public String getId() {
            return id;
        }

        public String getLink() {
            return link;
        }

        public String getEmail() {
            return email;
        }

        public String getPhone() {
            return phone;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getNickName() {
            return nickName;
        }

        public String getImageUrl() {
            return imageUrl;
        }
    }
}
