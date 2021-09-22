package org.mpashka.totemftc.api;

import io.netty.util.internal.StringUtil;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
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
                            "nick_name VARCHAR(30) NOT NULL" +
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
                            "image bytea" +
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

    public Uni<Integer> findById(String provider, String id, String email, String phone) {
        return selectBySocialNetwork
                .execute(Tuple.of(provider, id))
                .onItem().transform(this::find)
                .onItem().transformToUni(userId -> {
                    if (userId == null && Utils.notEmpty(email)) {
                        return selectByEmail
                                .execute(Tuple.of(email))
                                .onItem().transform(this::find);
                    }
                    return Uni.createFrom().item(userId);
                })
                .onItem().transformToUni(userId -> {
                    if (userId == null && Utils.notEmpty(phone)) {
                        return selectByPhone
                                .execute(Tuple.of(phone))
                                .onItem().transform(this::find);
                    }
                    return Uni.createFrom().item(userId);
                });
    }

    private Integer find(RowSet<Row> rows) {
        if (rows.size() > 0) {
            Row row = rows.iterator().next();
            Integer userId = row.getInteger("user_id");
            return userId;
        } else {
            return null;
        }
    }


}
