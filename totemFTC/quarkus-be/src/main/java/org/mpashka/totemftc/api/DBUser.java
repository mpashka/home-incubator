package org.mpashka.totemftc.api;

import io.netty.util.internal.StringUtil;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.PreparedQuery;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 *
 */
@ApplicationScoped
public class DBUser {
    private static final Logger log = LoggerFactory.getLogger(DBUser.class);

    private final PgPool client;
    private final boolean schemaCreate;
    private PreparedQuery<RowSet<Row>> selectBySocialNetwork;
    private PreparedQuery<RowSet<Row>> selectByEmail;
    private PreparedQuery<RowSet<Row>> selectByPhone;
    private PreparedQuery<RowSet<Row>> insertUser;
    private PreparedQuery<RowSet<Row>> insertSocialNetwork;
    private PreparedQuery<RowSet<Row>> insertEmail;
    private PreparedQuery<RowSet<Row>> insertPhone;
    private PreparedQuery<RowSet<Row>> insertImage;

    public DBUser(PgPool client, @ConfigProperty(name = "schema.create.user", defaultValue = "true") boolean schemaCreate) {
        log.debug("DBUser.new");
        this.client = client;
        this.schemaCreate = schemaCreate;
    }

    void onStart(@Observes StartupEvent ev) {
        if (schemaCreate) {
            initDb();
        }
        selectBySocialNetwork = client.preparedQuery("SELECT user_id " +
                "FROM social_network " +
                "WHERE network_id = $1 and id = $2");
        selectByEmail = client.preparedQuery("SELECT user_id " +
                "FROM user_email " +
                "WHERE email = $1");
        selectByPhone = client.preparedQuery("SELECT user_id " +
                "FROM user_phone " +
                "WHERE phone = $1");

        insertUser = client.preparedQuery("INSERT INTO user (first_name, last_name, nick_name) VALUES ($1, $2, $3) RETURNING user_id");
        insertSocialNetwork = client.preparedQuery("INSERT INTO social_network (network_id, id, user_id) VALUES ($1, $2, $3)");
        insertEmail = client.preparedQuery("INSERT INTO user_email (email, user_id, confirmed) VALUES ($1, $2, $3)");
        insertPhone = client.preparedQuery("INSERT INTO user_phone (phone, user_id, confirmed) VALUES ($1, $2, $3)");
        insertImage = client.preparedQuery("INSERT INTO user_image (user_id, image, content_type) VALUES ($1, $2, $3) RETURNING image_id");
    }

    private void initDb() {
        log.debug("Init database...");
        try {
            Uni.createFrom().item(1)
//                .flatMap(u -> client.query("DROP TABLE IF EXISTS location").execute())
                    .flatMap(r -> client.query("CREATE TABLE IF NOT EXISTS user (" +
                            "user_id SERIAL PRIMARY KEY, " +
                            "first_name VARCHAR(30) NOT NULL, " +
                            "last_name VARCHAR(30) NOT NULL, " +
                            "nick_name VARCHAR(30) NOT NULL," +
                            "primary_image INTEGER NULLABLE REFERENCES user_umage(image_id)" +
                            ");" +

                            "CREATE TABLE IF NOT EXISTS user_email (" +
                            "email VARCHAR(30) NOT NULL PRIMARY KEY," +
                            "user_id INTEGER NOT NULL REFERENCES user (user_id)," +
                            "confirmed boolean NOT NULL" +
                            ");" +

                            "CREATE TABLE IF NOT EXISTS user_phone (" +
                            "phone VARCHAR(14) NOT NULL PRIMARY KEY," +
                            "user_id INTEGER NOT NULL REFERENCES user (user_id)" +
                            "confirmed boolean NOT NULL" +
                            ");" +

                            "CREATE TABLE IF NOT EXISTS user_image (" +
                            "image_id SERIAL PRIMARY KEY," +
                            "user_id INTEGER NOT NULL REFERENCES user (user_id)," +
                            "image bytea," +
                            "content_type VARCHAR(20) NOT NULL" +
                            ");" +

                            "CREATE TABLE IF NOT EXISTS social_network (" +
                            "network_id VARCHAR(10) NOT NULL," +
                            "id VARCHAR(30) NOT NULL," +
                            "user_id INTEGER NOT NULL REFERENCES user (user_id)," +
                            "PRIMARY KEY (network_name,network_id))" +
                            "").execute())
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
     * Add social network and probably email and phone
     *
     * @param userId
     * @param provider
     * @param id
     * @param email
     * @param phone
     * @return
     */
    public Uni<Void> addSocialNetwork(int userId, String provider, String id, String email, String phone) {
        return insertSocialNetwork.execute(Tuple.of(provider, id, userId))
                .onItem().transformToUni(u -> {
                    return Utils.notEmpty(email)
                            ? insertEmail.execute(Tuple.of(email, userId, true))
                            : Uni.createFrom().item(null);
                })
                .onItemOrFailure().transformToUni((u, t) -> {
                    return Utils.notEmpty(phone)
                            ? insertPhone.execute(Tuple.of(phone, userId, true))
                            : Uni.createFrom().item(null);
                })
                .onItemOrFailure().transform((u, t) -> null);
    }

    /**
     * Return image id
     *
     * @param userId
     * @param image
     * @param contentType
     * @return
     */
    public Uni<Integer> addImage(int userId, byte[] image, String contentType) {
        return insertImage.execute(Tuple.of(userId, Buffer.buffer(image), contentType))
                .onItem().transform(rows -> rows.iterator().next().getInteger(1));
    }

    private UserSearchResult find(UserSearchType type, RowSet<Row> rows) {
        if (rows.size() > 0) {
            Row row = rows.iterator().next();
            int userId = row.getInteger("user_id");
            return new UserSearchResult(type, userId);
        } else {
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
