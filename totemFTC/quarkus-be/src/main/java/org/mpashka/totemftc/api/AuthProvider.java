package org.mpashka.totemftc.api;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.ext.web.client.WebClient;

import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;

/**
 * Probably useless abstraction class
 */
public abstract class AuthProvider {

    @Inject
    WebClient webClient;

    private String name;

    public AuthProvider(String name) {
        this.name = name;
    }

    public WebClient getWebClient() {
        return webClient;
    }

    public String getName() {
        return name;
    }

    public abstract String getAuthorizationEndpoint();

    public abstract Uni<WebResourceLogin.LoginState> processCallback(UriInfo uriInfo, WebResourceLogin.LoginState loginState, String client);

    public abstract Uni<UserInfo> readUserInfo(WebResourceLogin.LoginState loginState);

    static class UserInfo {
        String networkName;
        /** Social network user id */
        private String id;
        private String link;
        private String email;
        private String phone;
        /** Used to show and identify user social network */
        private String displayName;
        private String firstName;
        private String lastName;
        private String nickName;
        private String imageUrl;

        public UserInfo(String networkName, String id, String link, String email, String phone, String displayName, String firstName, String lastName, String nickName, String imageUrl) {
            this.networkName = networkName;
            this.id = id;
            this.link = link;
            this.email = email;
            this.phone = Utils.normalizePhone(phone);
            this.displayName = displayName;
            this.firstName = firstName;
            this.lastName = lastName;
            this.nickName = nickName;
            this.imageUrl = imageUrl;
        }

        public String getNetworkName() {
            return networkName;
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

        public String getDisplayName() {
            return displayName;
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
