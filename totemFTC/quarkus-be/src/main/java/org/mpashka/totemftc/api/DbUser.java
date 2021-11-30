package org.mpashka.totemftc.api;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.PreparedQuery;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowIterator;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.stream.StreamSupport;

import static org.mpashka.totemftc.api.Utils.firstNonBlank;
import static org.mpashka.totemftc.api.Utils.isBlank;
import static org.mpashka.totemftc.api.Utils.notBlank;

/**
 *
 */
@ApplicationScoped
public class DbUser {
    private static final Logger log = LoggerFactory.getLogger(DbUser.class);

    @Inject
    PgPool client;

    private PreparedQuery<RowSet<Row>> selectUser;
    private PreparedQuery<RowSet<Row>> selectUsers;
    private PreparedQuery<RowSet<Row>> selectTrainers;
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
    private PreparedQuery<RowSet<Row>> deleteUserSocialNetwork;

    @PostConstruct
    void init() {
        String selectUserSql = "SELECT u.*," +
                "   sn.social_networks," +
                "   e.emails," +
                "   p.phones," +
                "   i.images " +
                " FROM user_info u " +
                "   LEFT OUTER JOIN (SELECT user_id, array_agg(row_to_json(sn.*)) AS social_networks FROM user_social_network sn GROUP BY user_id) sn ON u.user_id = sn.user_id " +
                "   LEFT OUTER JOIN (SELECT user_id, array_agg(row_to_json(e.*)) AS emails FROM user_email e GROUP BY user_id) e ON u.user_id = e.user_id " +
                "   LEFT OUTER JOIN (SELECT user_id, array_agg(row_to_json(p.*)) AS phones FROM user_phone p GROUP BY user_id) p ON u.user_id = p.user_id " +
                "   LEFT OUTER JOIN (SELECT user_id, array_agg(json_build_object('image_id', i.image_id, 'content_type', i.content_type)) AS images FROM user_image i GROUP BY user_id) i ON u.user_id = i.user_id";
        selectUsers = client.preparedQuery(selectUserSql);
        selectTrainers = client.preparedQuery(selectUserSql +
                " WHERE cardinality(u.user_training_types) > 0" +
                "   AND u.user_type IN ('trainer', 'admin') ");
        selectUser = client.preparedQuery("SELECT * " +
                " FROM user_info u, " +
                "   (SELECT array_agg(row_to_json(sn.*)) AS social_networks FROM user_social_network sn WHERE user_id = $1) sn, " +
                "   (SELECT array_agg(row_to_json(e.*)) AS emails FROM user_email e WHERE user_id = $1) e, " +
                "   (SELECT array_agg(row_to_json(p.*)) AS phones FROM user_phone p WHERE user_id = $1) p, " +
                "   (SELECT array_agg(json_build_object('image_id', i.image_id, 'content_type', i.content_type)) AS images FROM user_image i WHERE user_id = $1) i " +
                " WHERE u.user_id = $1 ");

        selectBySocialNetwork = client.preparedQuery("SELECT user_id " +
                "FROM user_social_network " +
                "WHERE network_name = $1 and id = $2");
        selectByEmail = client.preparedQuery("SELECT user_id " +
                "FROM user_email " +
                "WHERE email = $1");
        selectByPhone = client.preparedQuery("SELECT user_id " +
                "FROM user_phone " +
                "WHERE phone = $1");

        insertUser = client.preparedQuery("INSERT INTO user_info (first_name, last_name, nick_name, user_type) VALUES ($1, $2, $3, $4) RETURNING user_id");
        insertSocialNetwork = client.preparedQuery("INSERT INTO user_social_network (network_name, id, user_id, link, display_name) VALUES ($1, $2, $3, $4, $5)");
        insertEmail = client.preparedQuery("INSERT INTO user_email (email, user_id, confirmed) VALUES ($1, $2, $3)");
        insertPhone = client.preparedQuery("INSERT INTO user_phone (phone, user_id, confirmed) VALUES ($1, $2, $3)");
        insertImage = client.preparedQuery("INSERT INTO user_image (user_id, image, content_type) VALUES ($1, $2, $3) RETURNING image_id");
        updateUser = client.preparedQuery("UPDATE user_info " +
                "SET first_name=$2, last_name=$3, nick_name=$4, primary_image=$5, user_type=$6, user_training_types=$7 " +
                "WHERE user_id=$1");
        updateMainImage = client.preparedQuery("UPDATE user_info SET primary_image=$2 WHERE user_id = $1");
        updateMainImageIfAbsent = client.preparedQuery("UPDATE user_info SET primary_image=$2 WHERE user_id=$1 AND primary_image is NULL");
        deleteUser = client.preparedQuery("DELETE FROM user_email WHERE user_id=$1;" +
                "DELETE FROM user_phone WHERE user_id=$1;" +
                "DELETE FROM user_image WHERE user_id=$1;" +
                "DELETE FROM user_social_network WHERE user_id=$1;" +
                "DELETE FROM user_info WHERE user_id=$1"
                );
        deleteUserSocialNetwork = client.preparedQuery("DELETE FROM user_social_network WHERE user_id=$1 and network_name=$2");
    }

    public Uni<UserSearchResult> findById(AuthProvider.UserInfo userInfo) {
        return selectBySocialNetwork
                .execute(Tuple.of(userInfo.getNetworkName(), userInfo.getId()))
                .onItem().transform(r -> find(UserSearchType.socialNetwork, r))
                .onItem().transformToUni(userId -> {
                    String email = userInfo.getEmail();
                    if (userId == null && Utils.notEmpty(email)) {
                        return selectByEmail
                                .execute(Tuple.of(email))
                                .onItem().transform(r -> find(UserSearchType.email, r));
                    } else {
                        return Uni.createFrom().item(userId);
                    }
                })
                .onItem().transformToUni(userId -> {
                    String phone = userInfo.getPhone();
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
        return selectUser.execute(Tuple.of(userId))
                .onItem().transform(rows -> {
                    RowIterator<Row> rowIterator = rows.iterator();
                    if (rowIterator.hasNext()) {
                        log.debug("User [{}] found", userId);
                        Row row = rowIterator.next();
                        return new EntityUser().loadFromDbFull(row);
                    } else {
                        log.debug("User [{}] not found", userId);
                        return null;
                    }
                })
                .onFailure().transform(e -> new RuntimeException("Error getUser", e))
                ;
    }

    public Uni<EntityUser[]> getAllUsers() {
        return getUsers(selectUsers);
    }

    public Uni<EntityUser[]> getTrainers() {
        return getUsers(selectTrainers);
    }

    private Uni<EntityUser[]> getUsers(PreparedQuery<RowSet<Row>> sql) {
        return sql.execute()
                .onItem().transform(set -> StreamSupport.stream(set.spliterator(), false)
                        .map(row -> new EntityUser().loadFromDbFull(row))
                        .toArray(EntityUser[]::new)
                )
                .onFailure().transform(e -> new RuntimeException("Error getAllUsers", e))
                ;
    }

    public Uni<Void> updateUser(EntityUser user) {
        return updateUser.execute(Tuple.from(Arrays.asList(user.userId, user.firstName, user.lastName, user.nickName,
                        user.primaryImage != null ? user.primaryImage.id : null, user.type.name(), user.trainingTypes)))
                .onFailure().transform(e -> new RuntimeException("Error update", e))
                .onItem().transform(u -> null)
                ;
    }

    public Uni<Void> deleteUserSocialNetwork(int userId, String socialNetworkName) {
        return deleteUserSocialNetwork.execute(Tuple.of(userId, socialNetworkName))
                .onFailure().transform(e -> new RuntimeException("Error delete social network", e))
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
    public Uni<Void> addUserSocialNetwork(int userId, AuthProvider.UserInfo userInfo) {
        String display;
        String displayName = userInfo.getDisplayName();
        String nickName = userInfo.getNickName();
        if (notBlank(displayName) && notBlank(nickName)) {
            display = displayName.equals(nickName) ? displayName : (displayName + " aka " + nickName);
        } else {
            display = firstNonBlank(displayName, nickName);
        }
        return insertSocialNetwork.execute(Tuple.of(userInfo.getNetworkName(), userInfo.getId(), userId, userInfo.getLink(), display))
                .onItem().transformToUni(u -> addEmail(userId, userInfo.getEmail()))
                .onItem().transformToUni(u -> addPhone(userId, userInfo.getPhone()))
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
        private EntityImage primaryImage;
        /** todo use set here */
        private UserType type;
        private String[] trainingTypes;
        private EntitySocialNetwork[] socialNetworks;
        private EntityPhone[] phones;
        private EntityEmail[] emails;
        private EntityImage[] images;


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

        public EntityUser setTrainingTypes(String[] trainingTypes) {
            this.trainingTypes = trainingTypes;
            return this;
        }

        public int getUserId() {
            return userId;
        }

        public EntityUser loadFromDb(Row row) {
            this.userId = row.getInteger("user_id");
            this.firstName = row.getString("first_name");
            this.lastName = row.getString("last_name");
            this.nickName = row.getString("nick_name");
            String userType = row.getString("user_type");
            try {
                this.type = UserType.valueOf(userType);
            } catch (IllegalArgumentException e) {
                log.warn("Unknown user type {}", userType, e);
                this.type = UserType.guest;
            }
            this.trainingTypes = row.getArrayOfStrings("user_training_types");
            return this;
        }

        public EntityUser loadFromDb(JsonObject row) {
            this.userId = row.getInteger("user_id");
            this.firstName = row.getString("first_name");
            this.lastName = row.getString("last_name");
            this.nickName = row.getString("nick_name");
            String userType = row.getString("user_type");
            try {
                this.type = UserType.valueOf(userType);
            } catch (IllegalArgumentException e) {
                log.warn("Unknown user type {}", userType, e);
                this.type = UserType.guest;
            }
            this.trainingTypes = row.getJsonArray("user_training_types").stream().toArray(String[]::new);
            return this;
        }

        public EntityUser loadFromDbFull(Row row) {
            loadFromDb(row);
            Integer primaryImageId = row.getInteger("primary_image");
            Object[] images = row.getArrayOfJsons("images");
            this.images = images == null ? null : Arrays.stream(images)
                    .map(ij -> {
                        EntityImage image = new EntityImage().loadFromDb((JsonObject) ij);
                        if (primaryImageId != null && image.getId() == primaryImageId) {
                            primaryImage = image;
                        }
                        return image;
                    })
                    .toArray(EntityImage[]::new);
            Object[] emails = row.getArrayOfJsons("emails");
            this.emails = emails == null ? null : Arrays.stream(emails)
                    .map(ej -> new EntityEmail().loadFromDb((JsonObject) ej))
                    .toArray(EntityEmail[]::new);
            Object[] phones = row.getArrayOfJsons("phones");
            this.phones = phones == null ? null : Arrays.stream(phones)
                    .map(pj -> new EntityPhone().loadFromDb((JsonObject) pj))
                    .toArray(EntityPhone[]::new);
            Object[] socialNetworks = row.getArrayOfJsons("social_networks");
            this.socialNetworks = socialNetworks == null ? null : Arrays.stream(socialNetworks)
                    .map(pj -> new EntitySocialNetwork().loadFromDb((JsonObject) pj))
                    .toArray(EntitySocialNetwork[]::new);
            return this;
        }

        public static class EntitySocialNetwork {
            private String networkName;
            private String id;
            private String link;
            private String displayName;

            public EntitySocialNetwork loadFromDb(JsonObject row) {
                this.networkName = row.getString("network_name");
                this.id = row.getString("id");
                this.link = row.getString("link");
                this.link = row.getString("display_name");
                return this;
            }
        }
        public static class EntityPhone {
            private String phone;
            private boolean confirmed;

            public EntityPhone loadFromDb(JsonObject row) {
                this.phone = row.getString("phone");
                this.confirmed = row.getBoolean("confirmed");
                return this;
            }
        }

        public static class EntityEmail {
            private String email;
            private boolean confirmed;

            public EntityEmail loadFromDb(JsonObject row) {
                this.email = row.getString("email");
                this.confirmed = row.getBoolean("confirmed");
                return this;
            }
        }

        public static class EntityImage {
            private int id;
            private String contentType;

            public EntityImage loadFromDb(JsonObject row) {
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
