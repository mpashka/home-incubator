package org.mpashka.totemftc.api;

import com.fasterxml.jackson.annotation.JsonFormat;
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

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.stream.StreamSupport;

/**
 * todo [!] This must be removed
 */
@Singleton
public class DbCrudVisit {
    private static final Logger log = LoggerFactory.getLogger(DbCrudVisit.class);

    @Inject
    PgPool client;

    @Inject
    @ConfigProperty(name = "db.schema.create.crud.visit", defaultValue = "true")
    boolean schemaCreate;

    private PreparedQuery<RowSet<Row>> select;
//    private PreparedQuery<RowSet<Row>> selectTrainerById;
    private PreparedQuery<RowSet<Row>> insert;
    private PreparedQuery<RowSet<Row>> update;
    private PreparedQuery<RowSet<Row>> delete;

    void onStart(@Observes StartupEvent ev) {
        if (schemaCreate) {
            createSchema();
        }
        select = client.preparedQuery("SELECT " +
                "* " +
                "FROM visits v " +
                "JOIN trainers t on v.trainer_id = t.trainer_id");
//        selectTrainerById = client.preparedQuery("SELECT * from trainers WHERE trainer_id = $1");
        insert = client.preparedQuery("INSERT INTO visits (trainer_id, visit_date, visit_comment) " +
                "VALUES ($1, $2, $3) RETURNING visit_id");
        update = client.preparedQuery("UPDATE visits " +
                "SET trainer_id=$1, visit_date=$2, visit_comment=$3 " +
                "WHERE visit_id=$1");
        delete = client.preparedQuery("DELETE FROM visits WHERE visit_id=$1");
    }

    private void createSchema() {
        log.debug("Init database...");
        try {
            Uni.createFrom().item(1)
                    .flatMap(r -> client.query("CREATE TABLE IF NOT EXISTS visits (" +
                            "visit_id SERIAL PRIMARY KEY, " +
                            "trainer_id INTEGER NOT NULL REFERENCES trainers (trainer_id), " +
                            "visit_date timestamp NOT NULL, " +
                            "visit_comment VARCHAR(30) NOT NULL " +
                            ");"
                    ).execute())
                    .await().indefinitely();
        } catch (Exception e) {
            log.error("Db init error", e);
        }
    }

    public Uni<EntityVisit[]> getAll() {
        return select
                .execute()
                .onItem().transform(set ->
                    StreamSupport.stream(set.spliterator(), false)
                            .map(r -> new EntityVisit().loadFromDb(r))
                            .toArray(EntityVisit[]::new)
                )
                .onFailure().transform(e -> new RuntimeException("Error getAll", e))
                ;
    }

/*
    public Uni<EntityVisit> getById(long id) {
        return selectTrainerById
                .execute(Tuple.of(id))
                .onItem().transform(r -> new EntityVisit().loadFromDb(r.iterator().next()))
                .onFailure().transform(e -> new RuntimeException("Error getById", e))
                ;
    }
*/

    /**
     *
     * @return trainer id
     */
    public Uni<Integer> add(EntityVisit entityVisit) {
        return insert.execute(Tuple.of(entityVisit.trainer.getTrainerId(), entityVisit.visitDate, entityVisit.visitComment))
                .onItem().transform(rows -> rows.iterator().next().getInteger("visit_id"))
                .onFailure().transform(e -> new RuntimeException("Error add", e))
                ;
    }

    public Uni<Void> update(EntityVisit entityVisit) {
        return update.execute(Tuple.of(entityVisit.trainer.getTrainerId(), entityVisit.visitDate, entityVisit.visitComment))
                .onFailure().transform(e -> new RuntimeException("Error update", e))
                .onItem().transform(u -> null)
                ;
    }

    public Uni<Void> delete(int id) {
        return delete.execute(Tuple.of(id))
                .onFailure().transform(e -> new RuntimeException("Error delete", e))
                .onItem().transform(u -> null)
                ;
    }

    public static class EntityVisit {
        private int visitId;
        private DbCrudTrainer.EntityTrainer trainer;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        private LocalDateTime visitDate;
        private String visitComment;

        public EntityVisit loadFromDb(Row row) {
            this.visitId = row.getInteger("visit_id");
            this.visitDate = row.getLocalDateTime("visit_date");
            this.visitComment = row.getString("visit_comment");
            this.trainer = new DbCrudTrainer.EntityTrainer()
                    .loadFromDb(row);
            return this;
        }
    }
}
