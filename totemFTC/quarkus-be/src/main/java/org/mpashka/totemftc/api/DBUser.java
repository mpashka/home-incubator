package org.mpashka.totemftc.api;

import io.quarkus.runtime.StartupEvent;
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
public class DBUser {
    private static final Logger log = LoggerFactory.getLogger(DBUser.class);

    private final PgPool client;
    private final boolean schemaCreate;
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
    private PreparedQuery<RowSet<Row>> updateMainImageIfAbsent;
    private PreparedQuery<RowSet<Row>> updateMainImage;

    public DBUser(PgPool client, @ConfigProperty(name = "db.schema.create.user", defaultValue = "true") boolean schemaCreate) {
        log.debug("DBUser.new");
        this.client = client;
        this.schemaCreate = schemaCreate;
    }

    void onStart(@Observes StartupEvent ev) {
        if (schemaCreate) {
            initDb();
        }

        selectUserShortById = client.preparedQuery("SELECT " +
                " u.first_name AS first_name, " +
                " u.last_name AS last_name, " +
                " u.nick_name AS nick_name, " +
                " u.primary_image AS primary_image, " +
                " user_image.image_id AS image_id, " +
                " user_image.content_type AS content_type " +
                "FROM user_info u " +
                "LEFT OUTER JOIN user_image ON u.primary_image = user_image.image_id " +
                "WHERE u.user_id = $1");
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

        insertUser = client.preparedQuery("INSERT INTO user_info (first_name, last_name, nick_name) VALUES ($1, $2, $3) RETURNING user_id");
        insertSocialNetwork = client.preparedQuery("INSERT INTO user_social_network (network_id, id, user_id, link) VALUES ($1, $2, $3, $4)");
        insertEmail = client.preparedQuery("INSERT INTO user_email (email, user_id, confirmed) VALUES ($1, $2, $3)");
        insertPhone = client.preparedQuery("INSERT INTO user_phone (phone, user_id, confirmed) VALUES ($1, $2, $3)");
        insertImage = client.preparedQuery("INSERT INTO user_image (user_id, image, content_type) VALUES ($1, $2, $3) RETURNING image_id");
        updateMainImage = client.preparedQuery("UPDATE user_info SET primary_image = $1 WHERE user_id = $2");
        updateMainImageIfAbsent = client.preparedQuery("UPDATE user_info SET primary_image = $1 WHERE user_id = $2 AND primary_image is NULL");
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
    public Uni<Integer> addUser(String firstName, String lastName, String nickName) {
        return insertUser.execute(Tuple.of(firstName, lastName, nickName))
                .onItem().transform(rows -> rows.iterator().next().getInteger("user_id"));
    }

    public Uni<EntityUser> getUser(int userId) {
        return selectUserShortById.execute(Tuple.of(userId))
                .onItem().transform(rows -> {
                    RowIterator<Row> rowIterator = rows.iterator();
                    if (rowIterator.hasNext()) {
                        log.debug("User [{}] found", userId);
                        Row row = rowIterator.next();
                        EntityUser user = new EntityUser()
                                .setUserId(userId)
                                .loadFromDb(row);

                        if (row.getInteger("image_id") != null) {
                            EntityUser.EntityImage image = new EntityUser.EntityImage()
                                    .loadFromDb(row);
                            user.setPrimaryImage(image);
                            log.debug("     Image {}", image.getId());
                        }
                        return user;
                    } else {
                        log.debug("User [{}] not found", userId);
                        return null;
                    }
                })
                .onFailure().transform(e -> new RuntimeException("Error getUser", e))
                ;
    }

    public Uni<EntityUser> getUserFull(int userId) {
        Tuple t = Tuple.of(userId);
        return selectUserFullById.execute(t)
                .onItem().transform(rows -> {
                    RowIterator<Row> rowIterator = rows.iterator();
                    return rowIterator.hasNext()
                            ? new EntityUser().setUserId(userId).loadFromDb(rowIterator.next())
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
                                });
                                user.setImages(images.toArray(EntityUser.EntityImage[]::new));
                            }).onItem().transform(u -> user));
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

/*
                .execute(Tuple.tuple(List.of(workTime, workProvider, time, provider, latitude, longitude, accuracy, battery,
                        miBattery, miSteps, miHeart, accelerometerAverage, accelerometerMaximum, accelerometerCount, activity)))
                .onFailure().invoke(e -> log.warn("Save location error", e))
                .onFailure().recoverWithNull();

    private Uni<?> saveLocations(List<LocationEntity> locationEntities) {
        if (locationEntities == null || locationEntities.isEmpty()) {
            log.debug("No locations");
            return Uni.createFrom().voidItem();
        }
        return Multi.createFrom().iterable(locationEntities).onItem()
                .transformToUni(l -> l.save(client))
                .merge().collect().asList()
                .invoke(l -> log.debug("Locations saved"));
    }

 */
}
