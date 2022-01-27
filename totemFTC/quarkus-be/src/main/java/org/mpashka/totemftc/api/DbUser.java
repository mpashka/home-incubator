package org.mpashka.totemftc.api;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.PreparedQuery;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowIterator;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.SqlResult;
import io.vertx.mutiny.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.mpashka.totemftc.api.Utils.firstNonBlank;
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
    private PreparedQuery<RowSet<Row>> updateUserNick;
    private PreparedQuery<RowSet<Row>> updateUserName;
    private PreparedQuery<RowSet<Row>> updateMainImageIfAbsent;
    private PreparedQuery<RowSet<Row>> updateMainImage;
    private PreparedQuery<RowSet<Row>> deleteUser;
    private PreparedQuery<RowSet<Row>> deleteUserSocialNetwork;
    private PreparedQuery<RowSet<Row>> createSession;
    private PreparedQuery<RowSet<Row>> selectSession;
    private PreparedQuery<RowSet<Row>> updateSession;
    private PreparedQuery<RowSet<Row>> deleteSession;
    private PreparedQuery<RowSet<Row>> cleanupSessions;

    @PostConstruct
    void init() {
        selectUsers = client.preparedQuery("SELECT * FROM user_info_full " +
                "ORDER BY last_name, first_name, nick_name, user_id ");
        selectTrainers = client.preparedQuery("SELECT * FROM user_info_full " +
                "WHERE cardinality(user_training_types) > 0 " +
                "   AND user_types @> ARRAY['trainer'::user_type_enum] " +
                "ORDER BY last_name, first_name, nick_name, user_id ");
        selectUser = client.preparedQuery("SELECT * FROM user_info_full " +
                "WHERE user_id = $1 ");

        selectBySocialNetwork = client.preparedQuery("SELECT * FROM user_info " +
                "WHERE user_id = " +
                "    (SELECT user_id FROM user_social_network WHERE network_name = $1 and id = $2)");
        selectByEmail = client.preparedQuery("SELECT * FROM user_info " +
                "WHERE user_id = " +
                "    (SELECT user_id FROM user_email WHERE email = $1)");
        selectByPhone = client.preparedQuery("SELECT * FROM user_info " +
                "WHERE user_id = " +
                "    (SELECT user_id FROM user_phone WHERE phone = $1)");

        insertUser = client.preparedQuery("INSERT INTO user_info (first_name, last_name, nick_name, user_types) VALUES ($1, $2, $3, $4) RETURNING user_id");
        insertSocialNetwork = client.preparedQuery("INSERT INTO user_social_network (network_name, id, user_id, link, display_name) VALUES ($1, $2, $3, $4, $5)");
        insertEmail = client.preparedQuery("INSERT INTO user_email (email, user_id, confirmed) VALUES ($1, $2, $3)");
        insertPhone = client.preparedQuery("INSERT INTO user_phone (phone, user_id, confirmed) VALUES ($1, $2, $3)");
        insertImage = client.preparedQuery("INSERT INTO user_image (user_id, image, content_type) VALUES ($1, $2, $3) RETURNING image_id");
        updateUser = client.preparedQuery("UPDATE user_info " +
                "SET first_name=$2, last_name=$3, nick_name=$4, primary_image=$5, user_types=$6, user_training_types=$7 " +
                "WHERE user_id=$1");
        updateUserNick = client.preparedQuery("UPDATE user_info " +
                "SET nick_name=$2 " +
                "WHERE user_id=$1");
        updateUserName = client.preparedQuery("UPDATE user_info " +
                "SET first_name=$2, last_name=$3, nick_name=$4, primary_image=$5 " +
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

        createSession = client.preparedQuery("INSERT INTO user_session (session_id, user_id, last_update) VALUES ($1, $2, $3)");
        selectSession = client.preparedQuery("SELECT * FROM user_session WHERE session_id=$1");
        updateSession = client.preparedQuery("UPDATE user_session SET last_update=$2 WHERE session_id=$1");
        deleteSession = client.preparedQuery("DELETE FROM user_session WHERE session_id=$1");
        cleanupSessions = client.preparedQuery("DELETE FROM user_session WHERE last_update < $1");
    }

    public Uni<UserSearchResult> findById(AuthProvider.UserInfo userInfo) {
        return selectBySocialNetwork
                .execute(Tuple.of(userInfo.getNetworkName(), userInfo.getId()))
                .onItem().transform(r -> find(UserSearchType.socialNetwork, r))
                .onItem().transformToUni(user -> {
                    String email = userInfo.getEmail();
                    if (user == null && Utils.notEmpty(email)) {
                        return selectByEmail
                                .execute(Tuple.of(email))
                                .onItem().transform(r -> find(UserSearchType.email, r));
                    } else {
                        return Uni.createFrom().item(user);
                    }
                })
                .onItem().transformToUni(user -> {
                    String phone = userInfo.getPhone();
                    if (user == null && Utils.notEmpty(phone)) {
                        return selectByPhone
                                .execute(Tuple.of(phone))
                                .onItem().transform(r -> find(UserSearchType.phone, r));
                    } else {
                        return Uni.createFrom().item(user);
                    }
                });
    }

    /**
     *
     * @return user id
     */
    public Uni<EntityUser> addUser(EntityUser user) {
        return insertUser.execute(Tuple.of(user.firstName, user.lastName, user.nickName, user.types.stream().map(Objects::toString).toArray(String[]::new)))
                .onItem().transform(rows -> {
                    Integer userId = rows.iterator().next().getInteger("user_id");
                    user.userId = userId;
                    return user;
                });
    }

    public Uni<EntityUser> getUser(int userId, boolean full) {
        return selectUser.execute(Tuple.of(userId))
                .onItem().transform(rows -> {
                    RowIterator<Row> rowIterator = rows.iterator();
                    if (rowIterator.hasNext()) {
                        log.debug("User [{}] found", userId);
                        Row row = rowIterator.next();
                        return full ? new EntityUser().loadFromDbFull(row) : new EntityUser().loadFromDb(row);
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
                        user.primaryImage != null ? user.primaryImage.id : null, user.types.stream().map(Objects::toString).toArray(String[]::new), user.trainingTypes)))
                .onFailure().transform(e -> new RuntimeException("Error update user", e))
                .onItem().transform(u -> null)
                ;
    }

    public Uni<Void> updateUserNick(int userId, String nick) {
        return updateUserNick.execute(Tuple.from(Arrays.asList(userId, nick)))
                .onFailure().transform(e -> new RuntimeException("Error update user nick", e))
                .onItem().transform(u -> null)
                ;
    }

    public Uni<Void> updateUserName(EntityUser user) {
        return updateUserName.execute(Tuple.from(Arrays.asList(user.userId, user.firstName, user.lastName, user.nickName,
                        user.primaryImage != null ? user.primaryImage.id : null)))
                .onFailure().transform(e -> new RuntimeException("Error update user name", e))
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
            EntityUser user = new EntityUser().loadFromDb(row);
            log.debug("User [{}] found by {}", user.userId, type);
            return new UserSearchResult(type, user);
        } else {
            log.debug("User not found by {}", type);
            return null;
        }
    }

    public Uni<WebSessionService.Session> getSession(String sessionId) {
        log.debug("Select session by id {}", sessionId);
        return selectSession.execute(Tuple.of(sessionId))
                .onFailure().transform(e -> new RuntimeException("Error getSession", e))
                .onItem().transformToUni(r -> {
                    RowIterator<Row> iterator = r.iterator();
                    if (!iterator.hasNext()) {
                        log.debug("    Session not found");
                        return Uni.createFrom().nullItem();
                    }
                    Row row = iterator.next();
                    int userId = row.getInteger("user_id");
                    OffsetDateTime sessionLastUpdate = row.getOffsetDateTime("last_update");
                    log.debug("Fetch user by id {}", userId);
                    return getUser(userId, false)
                            .map(u -> new WebSessionService.Session(sessionId, u, sessionLastUpdate));
                });
    }

    public Uni<Integer> createSession(WebSessionService.Session session) {
        return createSession.execute(Tuple.of(session.getSessionId(), session.getUserId(), session.getLastUpdate()))
                .onFailure().transform(e -> new RuntimeException("Error createSession", e))
                .onItem().transform(SqlResult::rowCount);
    }

    public Uni<Integer> updateSession(WebSessionService.Session session) {
        return updateSession.execute(Tuple.of(session.getSessionId(), session.getLastUpdate()))
                .onFailure().transform(e -> new RuntimeException("Error updateSession", e))
                .onItem().transform(SqlResult::rowCount);
    }

    /** @return number of removed sessions */
    public Uni<Integer> deleteSession(WebSessionService.Session session) {
        return deleteSession.execute(Tuple.of(session.getSessionId()))
                .onFailure().transform(e -> new RuntimeException("Error deleteSessions", e))
                .onItem().transform(SqlResult::rowCount);
    }

    public Uni<Integer> cleanupSessions(OffsetDateTime lastUpdate) {
        return cleanupSessions.execute(Tuple.of(lastUpdate))
                .onFailure().transform(e -> new RuntimeException("Error deleteSessions", e))
                .onItem().transform(SqlResult::rowCount);
    }

    public enum UserSearchType {
        socialNetwork, email, phone
    }

    public static class UserSearchResult {
        private UserSearchType type;
        private EntityUser user;

        public UserSearchResult(UserSearchType type, EntityUser user) {
            this.type = type;
            this.user = user;
        }

        public UserSearchType getType() {
            return type;
        }

        public EntityUser getUser() {
            return user;
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
        private EnumSet<UserType> types;
        private String[] trainingTypes;
        private EntitySocialNetwork[] socialNetworks;
        private EntityPhone[] phones;
        private EntityEmail[] emails;
        private EntityImage[] images;


        public int getUserId() {
            return userId;
        }

        public EntityUser setUserId(int userId) {
            this.userId = userId;
            return this;
        }

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

        public EnumSet<UserType> getTypes() {
            return types;
        }

        public EntityUser setTypes(EnumSet<UserType> types) {
            this.types = types;
            return this;
        }

        public EntityUser setTrainingTypes(String[] trainingTypes) {
            this.trainingTypes = trainingTypes;
            return this;
        }

        public String getNickName() {
            return nickName;
        }

        public EntityUser loadFromDb(Row row) {
            this.userId = row.getInteger("user_id");
            this.firstName = row.getString("first_name");
            this.lastName = row.getString("last_name");
            this.nickName = row.getString("nick_name");
            String[] userTypes = row.getArrayOfStrings("user_types");
            try {
                this.types = Arrays.stream(userTypes).map(UserType::valueOf).collect(Collectors.toCollection(() -> EnumSet.noneOf(UserType.class)));
            } catch (IllegalArgumentException e) {
                log.warn("Unknown user type {}", userTypes, e);
                this.types = EnumSet.noneOf(UserType.class);
            }
            this.trainingTypes = row.getArrayOfStrings("user_training_types");
            return this;
        }

        public EntityUser loadFromDb(JsonObject row) {
            this.userId = row.getInteger("user_id");
            this.firstName = row.getString("first_name");
            this.lastName = row.getString("last_name");
            this.nickName = row.getString("nick_name");
            JsonArray userTypes = row.getJsonArray("user_types");
            try {
                this.types = userTypes.stream().map(s -> UserType.valueOf(s.toString())).collect(Collectors.toCollection(() -> EnumSet.noneOf(UserType.class)));
            } catch (IllegalArgumentException e) {
                log.warn("Unknown user type {}", userTypes, e);
                this.types = EnumSet.noneOf(UserType.class);
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
/*
            Object[] emails = row.getArrayOfJsons("emails");
            todo [!] temporarily hardcoded off for demo
            this.emails = emails == null ? null : Arrays.stream(emails)
                    .map(ej -> new EntityEmail().loadFromDb((JsonObject) ej))
                    .toArray(EntityEmail[]::new);
            Object[] phones = row.getArrayOfJsons("phones");
            this.phones = phones == null ? null : Arrays.stream(phones)
                    .map(pj -> new EntityPhone().loadFromDb((JsonObject) pj))
                    .toArray(EntityPhone[]::new);
*/
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
/*
                todo [!] temporarily hardcoded off
                this.id = row.getString("id");
*/
                this.link = row.getString("link");
                this.displayName = row.getString("display_name");
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
        user, trainer, admin
    }
}
