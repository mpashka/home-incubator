package org.mpashka.totemftc.api;

import com.fasterxml.jackson.annotation.JsonIgnore;import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.PreparedQuery;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowIterator;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

/**
 *
 */
@ApplicationScoped
public class DbUser {
    private static final Logger log = LoggerFactory.getLogger(DbUser.class);

    private final PgPool client;
    private final boolean schemaCreate;
    private PreparedQuery<RowSet<Row>> selectUsers;
    private PreparedQuery<RowSet<Row>> selectUserShortById;
    private PreparedQuery<RowSet<Row>> selectUserFullById;
    private PreparedQuery<RowSet<Row>> selectSocialNetworkByUserId;
    private PreparedQuery<RowSet<Row>> selectEmailByUserId;
    private PreparedQuery<RowSet<Row>> selectPhoneByUserId;
    private PreparedQuery<RowSet<Row>> selectImageByUserId;
    private PreparedQuery<RowSet<Row>> selectBySocialNetwork;
    private PreparedQuery<RowSet<Row>> selectByEmail;
    private PreparedQuery<RowSet<Row>> selectByPhone;
    private PreparedQuery<RowSet<Row>> insertUser;
    private PreparedQuery<RowSet<Row>> insertSocialNetwork;
    private PreparedQuery<RowSet<Row>> insertEmail;
    private PreparedQuery<RowSet<Row>> insertPhone;
    private PreparedQuery<RowSet<Row>> insertImage;
    private PreparedQuery<RowSet<Row>> updateUser;
    private PreparedQuery<RowSet<Row>> updateMainImageIfAbsent;
    private PreparedQuery<RowSet<Row>> updateMainImage;
    private PreparedQuery<RowSet<Row>> deleteUser;

    public DbUser(PgPool client, @ConfigProperty(name = "db.schema.create.user", defaultValue = "true") boolean schemaCreate) {
        log.debug("DbUser.new");
        this.client = client;
        this.schemaCreate = schemaCreate;
    }

    void onStart(@Observes StartupEvent ev) {
        if (schemaCreate) {
            initDb();
        }

        String selectUserSql = "SELECT * FROM user_info u " +
                "LEFT OUTER JOIN user_image ON u.primary_image = user_image.image_id ";
        selectUsers = client.preparedQuery(selectUserSql);
        selectUserShortById = client.preparedQuery(selectUserSql + "WHERE u.user_id = $1");
        selectUserFullById = client.preparedQuery("SELECT * from user_info WHERE user_id = $1");
        selectSocialNetworkByUserId = client.preparedQuery("SELECT * FROM user_social_network WHERE user_id = $1 ORDER BY network_id");
        selectEmailByUserId = client.preparedQuery("SELECT * FROM user_email WHERE user_id = $1 ORDER BY email");
        selectPhoneByUserId = client.preparedQuery("SELECT * FROM user_phone WHERE user_id = $1 ORDER BY phone");
        selectImageByUserId = client.preparedQuery("SELECT image_id FROM user_image WHERE user_id = $1 ORDER BY image_id");

        selectBySocialNetwork = client.preparedQuery("SELECT user_id " +
                "FROM user_social_network " +
                "WHERE network_id = $1 and id = $2");
        selectByEmail = client.preparedQuery("SELECT user_id " +
                "FROM user_email " +
                "WHERE email = $1");
        selectByPhone = client.preparedQuery("SELECT user_id " +
                "FROM user_phone " +
                "WHERE phone = $1");

        insertUser = client.preparedQuery("INSERT INTO user_info (first_name, last_name, nick_name, user_type) VALUES ($1, $2, $3, $4) RETURNING user_id");
        insertSocialNetwork = client.preparedQuery("INSERT INTO user_social_network (network_id, id, user_id, link) VALUES ($1, $2, $3, $4)");
        insertEmail = client.preparedQuery("INSERT INTO user_email (email, user_id, confirmed) VALUES ($1, $2, $3)");
        insertPhone = client.preparedQuery("INSERT INTO user_phone (phone, user_id, confirmed) VALUES ($1, $2, $3)");
        insertImage = client.preparedQuery("INSERT INTO user_image (user_id, image, content_type) VALUES ($1, $2, $3) RETURNING image_id");
        updateUser = client.preparedQuery("UPDATE user_info SET first_name=$2, last_name=$3, nick_name=$4, primary_image=$5, user_type=$6  WHERE user_id=$1");
        updateMainImage = client.preparedQuery("UPDATE user_info SET primary_image=$2 WHERE user_id = $1");
        updateMainImageIfAbsent = client.preparedQuery("UPDATE user_info SET primary_image=$2 WHERE user_id=$1 AND primary_image is NULL");
        deleteUser = client.preparedQuery("DELETE FROM user_email WHERE user_id=$1;" +
                "DELETE FROM user_phone WHERE user_id=$1;" +
                "DELETE FROM user_image WHERE user_id=$1;" +
                "DELETE FROM user_social_network WHERE user_id=$1;" +
                "DELETE FROM user_info WHERE user_id=$1"
                );
    }

    private void initDb() {
        log.debug("Init database...");
        try {
            Uni.createFrom().item(1)
                    .flatMap(r -> client.query("CREATE TABLE IF NOT EXISTS user_info (" +
                            "user_id SERIAL PRIMARY KEY, " +
                            "first_name VARCHAR(30) NULL, " +
                            "last_name VARCHAR(30) NULL, " +
                            "nick_name VARCHAR(30) NULL," +
                            "primary_image INTEGER NULL" + /* REFERENCES user_umage(image_id) */
                            ");" +

                            "CREATE TABLE IF NOT EXISTS user_email (" +
                            "email VARCHAR(30) NOT NULL PRIMARY KEY," +
                            "user_id INTEGER NOT NULL REFERENCES user_info (user_id)," +
                            "confirmed boolean NOT NULL" +
                            ");" +

                            "CREATE TABLE IF NOT EXISTS user_phone (" +
                            "phone VARCHAR(14) NOT NULL PRIMARY KEY," +
                            "user_id INTEGER NOT NULL REFERENCES user_info (user_id), " +
                            "confirmed boolean NOT NULL" +
                            ");" +

                            "CREATE TABLE IF NOT EXISTS user_image (" +
                            "image_id SERIAL PRIMARY KEY," +
                            "user_id INTEGER NOT NULL REFERENCES user_info (user_id)," +
                            "image bytea," +
                            "content_type VARCHAR(20) NOT NULL" +
                            ");" +

                            "CREATE TABLE IF NOT EXISTS user_social_network (" +
                            "network_id VARCHAR(10) NOT NULL," +
                            "id VARCHAR(30) NOT NULL," +
                            "user_id INTEGER NOT NULL REFERENCES user_info (user_id)," +
                            "link VARCHAR(400) NULL," +
                            "PRIMARY KEY (network_id,id)" +
                            ");"
                    ).execute())
                    .await().indefinitely();
        } catch (Exception e) {
            log.error("Db init error", e);
        }
    }

    public Uni<UserSearchResult> findById(String provider, String id, String email, String phone) {
        return selectBySocialNetwork
                .execute(Tuple.of(provider, id))
                .onItem().transform(r -> find(UserSearchType.socialNetwork, r))
                .onItem().transformToUni(userId -> {
                    if (userId == null && Utils.notEmpty(email)) {
                        return selectByEmail
                                .execute(Tuple.of(email))
                                .onItem().transform(r -> find(UserSearchType.email, r));
                    } else {
                        return Uni.createFrom().item(userId);
                    }
                })
                .onItem().transformToUni(userId -> {
                    if (userId == null && Utils.notEmpty(phone)) {
                        return selectByPhone
                                .execute(Tuple.of(phone))
                                .onItem().transform(r -> find(UserSearchType.phone, r));
                    } else {
                        return Uni.createFrom().item(userId);
                    }
                });
    }

    /**
     *
     * @return user id
     */
    public Uni<Integer> addUser(EntityUser user) {
        return insertUser.execute(Tuple.of(user.firstName, user.lastName, user.nickName, user.type.name()))
                .onItem().transform(rows -> rows.iterator().next().getInteger("user_id"));
    }

    public Uni<EntityUser> getUser(int userId) {
        return selectUserShortById.execute(Tuple.of(userId))
                .onItem().transform(rows -> {
                    RowIterator<Row> rowIterator = rows.iterator();
                    if (rowIterator.hasNext()) {
                        log.debug("User [{}] found", userId);
                        Row row = rowIterator.next();
                        return loadUserSimple(row);
                    } else {
                        log.debug("User [{}] not found", userId);
                        return null;
                    }
                })
                .onFailure().transform(e -> new RuntimeException("Error getUser", e))
                ;
    }

    public Uni<EntityUser[]> getAllUsers() {
        return selectUsers.execute()
                .onItem().transform(set -> StreamSupport.stream(set.spliterator(), false)
                        .map(this::loadUserSimple)
                        .toArray(EntityUser[]::new)
                )
                .onFailure().transform(e -> new RuntimeException("Error getAllUsers", e))
                ;
    }

    private EntityUser loadUserSimple(Row row) {
        EntityUser user = new EntityUser().loadFromDb(row);
        if (row.getInteger("image_id") != null) {
            EntityUser.EntityImage image = new EntityUser.EntityImage()
                    .loadFromDb(row);
            user.setPrimaryImage(image);
            log.debug("     Image {}", image.getId());
        }
        return user;
    }

    public Uni<EntityUser> getUserFull(int userId) {
        Tuple t = Tuple.of(userId);
        return selectUserFullById.execute(t)
                .onItem().transform(rows -> {
                    RowIterator<Row> rowIterator = rows.iterator();
                    return rowIterator.hasNext()
                            ? new EntityUser().loadFromDb(rowIterator.next())
                            : null;
                })
                .onItem().transformToUni(user ->
                        selectSocialNetworkByUserId.execute(t)
                                .onItem().invoke(rows -> {
                                    EntityUser.EntitySocialNetwork[] socialNetworks = StreamSupport.stream(rows.spliterator(), false)
                                            .map(row -> new EntityUser.EntitySocialNetwork().loadFromDb(row))
                                            .toArray(EntityUser.EntitySocialNetwork[]::new);
                                    user.setSocialNetworks(socialNetworks);
                                }).onItem().transform(u -> user))
                .onItem().transformToUni(user ->
                        selectPhoneByUserId.execute(t)
                                .onItem().invoke(phoneRows -> {
                                    EntityUser.EntityPhone[] phones = StreamSupport.stream(phoneRows.spliterator(), false)
                                            .map(phoneRow -> new EntityUser.EntityPhone().loadFromDb(phoneRow))
                                            .toArray(EntityUser.EntityPhone[]::new);
                                    user.setPhones(phones);
                                }).onItem().transform(u -> user))
                .onItem().transformToUni(user ->
                        selectEmailByUserId.execute(t)
                                .onItem().invoke(emailRows -> {
                                    EntityUser.EntityEmail[] emails = StreamSupport.stream(emailRows.spliterator(), false)
                                            .map(emailRow -> new EntityUser.EntityEmail().loadFromDb(emailRow))
                                            .toArray(EntityUser.EntityEmail[]::new);
                                    user.setEmails(emails);
                                }).onItem().transform(u -> user))
                .onItem().transformToUni(user ->
                    selectImageByUserId.execute(t)
                            .onItem().invoke(imageRows -> {
                                List<EntityUser.EntityImage> images = new ArrayList<>();
                                imageRows.forEach(imageRow -> {
                                    EntityUser.EntityImage image = new EntityUser.EntityImage()
                                            .loadFromDb(imageRow);
                                    if (user.getPrimaryImageId() != null && image.getId() == user.getPrimaryImageId()) {
                                        user.setPrimaryImage(image);
                                    }
                                    images.add(image);
                                });
                                user.setImages(images.toArray(EntityUser.EntityImage[]::new));
                            }).onItem().transform(u -> user))
                .onFailure().transform(e -> new RuntimeException("Error getUserFull", e));
    }

    public Uni<Void> updateUser(EntityUser user) {
        return updateUser.execute(Tuple.of(user.userId, user.firstName, user.lastName, user.nickName,
                        user.primaryImage != null ? user.primaryImage.id : null, user.type.name()))
                .onFailure().transform(e -> new RuntimeException("Error update", e))
                .onItem().transform(u -> null)
                ;
    }

    public Uni<Void> deleteUser(int userId) {
        return deleteUser.execute(Tuple.of(userId))
                .onFailure().transform(e -> new RuntimeException("Error delete", e))
                .onItem().transform(u -> null)
                ;
    }

    /**
     * Add social network and probably email and phone
     */
    public Uni<Void> addSocialNetwork(int userId, String provider, String id, String link, String email, String phone) {
        return insertSocialNetwork.execute(Tuple.of(provider, id, userId, link))
                .onItem().transformToUni(u -> addEmail(userId, email))
                .onItem().transformToUni(u -> addPhone(userId, phone))
                .onFailure().transform(e -> new RuntimeException("Error addSocialNetwork", e))
                .onItem().transform(u -> null)
                ;
    }

    /**
     * @return true if email was added, false if it was already present
     */
    public Uni<Boolean> addEmail(int userId, String email) {
        return (Utils.notEmpty(email)
                ? insertEmail
                .execute(Tuple.of(email, userId, true))
                : Uni.createFrom().item(false)
        ).onItemOrFailure().transform((r, e) -> e == null);
    }

    /**
     * @return true if email was added, false if it was already present
     */
    public Uni<Boolean> addPhone(int userId, String phone) {
        return (Utils.notEmpty(phone)
                ? insertPhone
                .execute(Tuple.of(phone, userId, true))
                : Uni.createFrom().item(false)
        ).onItemOrFailure().transform((r, e) -> e == null);
    }

    /**
     * @return image id
     */
    public Uni<Integer> addImage(int userId, Buffer image, String contentType) {
        return insertImage.execute(Tuple.of(userId, image.getDelegate(), contentType))
                .onItem().transform(rows -> rows.iterator().next().getInteger("image_id"));
    }

    /**
     *
     * @param ifAbsent update main image if there was no main image
     * @return true if image was set, false if {@param ifAbsent} was set and record already had main image
     */
    public Uni<Boolean> setMainImage(int userId, int imageId, boolean ifAbsent) {
        return (ifAbsent ? updateMainImageIfAbsent : updateMainImage).execute(Tuple.of(userId, imageId))
                .onItem().transform(r -> r.rowCount() > 0);
    }

    private UserSearchResult find(UserSearchType type, RowSet<Row> rows) {
        if (rows.size() > 0) {
            Row row = rows.iterator().next();
            int userId = row.getInteger("user_id");
            log.debug("User [{}] found by {}", userId, type);
            return new UserSearchResult(type, userId);
        } else {
            log.debug("User not found by {}", type);
            return null;
        }
    }

    public enum UserSearchType {
        socialNetwork, email, phone
    }

    public static class UserSearchResult {
        private UserSearchType type;
        private int userId;

        public UserSearchResult(UserSearchType type, int userId) {
            this.type = type;
            this.userId = userId;
        }

        public UserSearchType getType() {
            return type;
        }

        public int getUserId() {
            return userId;
        }
    }


    //@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static class EntityUser {
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
        private UserType type;

        public EntityUser setFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public EntityUser setLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public EntityUser setNickName(String nickName) {
            this.nickName = nickName;
            return this;
        }

        public EntityUser setType(UserType type) {
            this.type = type;
            return this;
        }

        public int getUserId() {
            return userId;
        }

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

        public EntityUser loadFromDb(Row row) {
            this.userId = row.getInteger("user_id");
            this.firstName = row.getString("first_name");
            this.lastName = row.getString("last_name");
            this.nickName = row.getString("nick_name");
            this.primaryImageId = row.getInteger("primary_image");
            String userType = row.getString("user_type");
            try {
                this.type = UserType.valueOf(userType);
            } catch (IllegalArgumentException e) {
                log.warn("Unknown user type {}", userType, e);
                this.type = UserType.guest;
            }
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

    public enum UserType {
        guest, user, trainer, admin
    }
}
