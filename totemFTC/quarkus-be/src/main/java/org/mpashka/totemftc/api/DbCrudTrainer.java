package org.mpashka.totemftc.api;

import io.quarkus.runtime.StartupEvent;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.PreparedQuery;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.stream.StreamSupport;

@Singleton
public class DbCrudTrainer {
    private static final Logger log = LoggerFactory.getLogger(DbCrudTrainer.class);

    @Inject
    PgPool client;

    @Inject
    @ConfigProperty(name = "db.schema.create.crud.trainer", defaultValue = "true")
    boolean schemaCreate;

    private PreparedQuery<RowSet<Row>> selectTrainers;
    private PreparedQuery<RowSet<Row>> selectTrainerById;
    private PreparedQuery<RowSet<Row>> insertTrainer;
    private PreparedQuery<RowSet<Row>> updateTrainer;
    private PreparedQuery<RowSet<Row>> deleteTrainer;

    void onStart(@Observes StartupEvent ev) {
        if (schemaCreate) {
            createSchema();
        }
        selectTrainers = client.preparedQuery("SELECT * from trainers");
        selectTrainerById = client.preparedQuery("SELECT * from trainers WHERE trainer_id = $1");
        insertTrainer = client.preparedQuery("INSERT INTO trainers (trainer_name) VALUES ($1) RETURNING trainer_id");
        updateTrainer = client.preparedQuery("UPDATE trainers SET trainer_name=$2 WHERE trainer_id=$1");
        deleteTrainer = client.preparedQuery("DELETE FROM trainers WHERE trainer_id=$1");
    }

    private void createSchema() {
        log.debug("Init database...");
        try {
            Uni.createFrom().item(1)
                    .flatMap(r -> client.query("CREATE TABLE IF NOT EXISTS trainers (" +
                            "trainer_id SERIAL PRIMARY KEY, " +
                            "trainer_name VARCHAR(40) NOT NULL " +
                            ");"
                    ).execute())
                    .await().indefinitely();
        } catch (Exception e) {
            log.error("Db init error", e);
        }
    }

    public Uni<EntityTrainer[]> getAll() {
        return selectTrainers
                .execute()
                .onItem().transform(set ->
                    StreamSupport.stream(set.spliterator(), false)
                            .map(r -> new EntityTrainer().loadFromDb(r))
                            .toArray(EntityTrainer[]::new)
                )
                .onFailure().transform(e -> new RuntimeException("Error getAll", e))
                ;
    }

    public Uni<EntityTrainer> getById(long id) {
        return selectTrainerById
                .execute(Tuple.of(id))
                .onItem().transform(r -> new EntityTrainer().loadFromDb(r.iterator().next()))
                .onFailure().transform(e -> new RuntimeException("Error getById", e))
                ;
    }

    /**
     *
     * @return trainer id
     */
    public Uni<Integer> add(EntityTrainer trainer) {
        return insertTrainer.execute(Tuple.of(trainer.trainerName))
                .onItem().transform(rows -> rows.iterator().next().getInteger("trainer_id"))
                .onFailure().transform(e -> new RuntimeException("Error add", e))
                ;
    }

    public Uni<Void> update(EntityTrainer trainer) {
        return updateTrainer.execute(Tuple.of(trainer.trainerId, trainer.trainerName))
                .onFailure().transform(e -> new RuntimeException("Error update", e))
                .onItem().transform(u -> null)
                ;
    }

    public Uni<Void> delete(int id) {
        return deleteTrainer.execute(Tuple.of(id))
                .onFailure().transform(e -> new RuntimeException("Error delete", e))
                .onItem().transform(u -> null)
                ;
    }

    public static class EntityTrainer {
        private int trainerId;
        private String trainerName;

        public EntityTrainer loadFromDb(Row row) {
            this.trainerId = row.getInteger("trainer_id");
            this.trainerName = row.getString("trainer_name");
            return this;
        }
    }
}
