package org.mpashka.totemftc.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vertx.mutiny.sqlclient.Row;

public class EntityUser {
//    @JsonProperty("work_time")
    private int userId;
    private String firstName;
    private String lastName;
    private String nickName;
    @JsonIgnore
    private Integer primaryImageId;
    private EntityImage primaryImage;
    private EntitySocialNetwork[] socialNetworks;
    private EntityPhone[] phones;
    private EntityEmail[] emails;
    private EntityImage[] images;

    public Integer getPrimaryImageId() {
        return primaryImageId;
    }

    public void setPrimaryImage(EntityImage primaryImage) {
        this.primaryImage = primaryImage;
    }

    public void setSocialNetworks(EntitySocialNetwork[] socialNetworks) {
        this.socialNetworks = socialNetworks;
    }

    public void setPhones(EntityPhone[] phones) {
        this.phones = phones;
    }

    public void setEmails(EntityEmail[] emails) {
        this.emails = emails;
    }

    public void setImages(EntityImage[] images) {
        this.images = images;
    }

    public EntityUser setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public EntityUser loadFromDb(Row row) {
        this.firstName = row.getString("first_name");
        this.lastName = row.getString("last_name");
        this.nickName = row.getString("nick_name");
        this.primaryImageId = row.getInteger("primary_image");
        return this;
    }

    public static class EntitySocialNetwork {
        private String networkId;
        private String id;
        private String link;

        public EntitySocialNetwork loadFromDb(Row row) {
            this.networkId = row.getString("network_id");
            this.id = row.getString("id");
            this.link = row.getString("link");
            return this;
        }
    }
    public static class EntityPhone {
        private String phone;
        private boolean confirmed;

        public EntityPhone loadFromDb(Row row) {
            this.phone = row.getString("phone");
            this.confirmed = row.getBoolean("confirmed");
            return this;
        }
    }

    public static class EntityEmail {
        private String email;
        private boolean confirmed;

        public EntityEmail loadFromDb(Row row) {
            this.email = row.getString("email");
            this.confirmed = row.getBoolean("confirmed");
            return this;
        }
    }

    public static class EntityImage {
        private int id;
        private String contentType;

        public EntityImage loadFromDb(Row row) {
            this.id = row.getInteger("image_id");
            this.contentType = row.getString("content_type");
            return this;
        }

        public int getId() {
            return id;
        }
    }

}
