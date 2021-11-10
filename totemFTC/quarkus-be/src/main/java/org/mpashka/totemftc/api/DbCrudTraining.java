package org.mpashka.totemftc.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.PreparedQuery;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Singleton
public class DbCrudTraining {
    private static final Logger log = LoggerFactory.getLogger(DbCrudTraining.class);

    @Inject
    PgPool client;

    private PreparedQuery<RowSet<Row>> select;
    private PreparedQuery<RowSet<Row>> selectByDate;
    private PreparedQuery<RowSet<Row>> selectByDateInterval;
//    private PreparedQuery<RowSet<Row>> selectByDateIntervalExcludeVisits;
    private PreparedQuery<RowSet<Row>> selectTrainingTypes;
    private PreparedQuery<RowSet<Row>> insert;
    private PreparedQuery<RowSet<Row>> update;
    private PreparedQuery<RowSet<Row>> delete;

    @PostConstruct
    void init() {
        select = client.preparedQuery("SELECT * from training t " +
                "JOIN user_info u ON t.trainer = u.user_id " +
                "JOIN training_type tt on t.training_type = tt.training_type " +
                "ORDER BY t.training_time");
        selectByDate = client.preparedQuery("SELECT * from training t " +
                "JOIN user_info u ON t.trainer = u.user_id " +
                "JOIN training_type tt on t.training_type = tt.training_type " +
                "WHERE date(t.training_time) = $1 " +
                "ORDER BY t.training_time");
        selectByDateInterval = client.preparedQuery("SELECT * from training t " +
                "JOIN user_info u ON t.trainer = u.user_id " +
                "JOIN training_type tt on t.training_type = tt.training_type " +
                "WHERE t.training_time >= $1 AND t.training_time <= $2 " +
                "ORDER BY t.training_time");
/*
        selectByDateIntervalExcludeVisits = client.preparedQuery("SELECT t.*,u.*,tt.* from training t " +
                "JOIN user_info u ON t.trainer = u.user_id " +
                "JOIN training_type tt on t.training_type = tt.training_type " +
                "LEFT OUTER JOIN training_visit tv on t.training_id = tv.training_id " +
                "WHERE t.training_time >= $1 AND t.training_time <= $2 " +
                "   AND tv.user_id=$3" +
                "   AND tv.training_id IS NULL " +
                "ORDER BY t.training_time");
*/
        selectTrainingTypes = client.preparedQuery("SELECT * FROM training_type");
        insert = client.preparedQuery("INSERT INTO training (training_time, trainer, training_type) VALUES ($1, $2, $3) RETURNING training_id");
        update = client.preparedQuery("UPDATE training SET training_time=$2, trainer=$3, training_type=$4 WHERE training_id=$1");
        delete = client.preparedQuery("DELETE FROM training WHERE training_id=$1");
    }

    public Uni<Entity[]> getAll() {
        return select
                .execute()
                .onItem().transform(set ->
                    StreamSupport.stream(set.spliterator(), false)
                            .map(r -> new Entity().loadFromDb(r))
                            .toArray(Entity[]::new)
                )
                .onFailure().transform(e -> new RuntimeException("Error getAll", e))
                ;
    }

    public Uni<Entity[]> getByDate(LocalDate date) {
        return selectByDate
                .execute(Tuple.of(date))
                .onItem().transform(set ->
                    StreamSupport.stream(set.spliterator(), false)
                            .map(r -> new Entity().loadFromDb(r))
                            .toArray(Entity[]::new)
                )
                .onFailure().transform(e -> new RuntimeException("Error getByDate", e))
                ;
    }

    public Uni<Entity[]> getByDateInterval(LocalDateTime from, LocalDateTime to) {
        return selectByDateInterval
                .execute(Tuple.of(from, to))
                .onItem().transform(set ->
                    StreamSupport.stream(set.spliterator(), false)
                            .map(r -> new Entity().loadFromDb(r))
                            .toArray(Entity[]::new)
                )
                .onFailure().transform(e -> new RuntimeException("Error getByDateInterval", e))
                ;
    }

/*
    public Uni<Entity[]> getByDateIntervalExcludeVisits(LocalDateTime from, LocalDateTime to, int userId) {
        return selectByDateIntervalExcludeVisits
                .execute(Tuple.of(from, to, userId))
                .onItem().transform(set ->
                    StreamSupport.stream(set.spliterator(), false)
                            .map(r -> new Entity().loadFromDb(r))
                            .toArray(Entity[]::new)
                )
                .onFailure().transform(e -> new RuntimeException("Error getByDateInterval", e))
                ;
    }
*/

    public Uni<EntityTrainingType[]> getTrainingTypes() {
        return selectTrainingTypes
                .execute()
                .onItem().transform(set ->
                    StreamSupport.stream(set.spliterator(), false)
                            .map(r -> new EntityTrainingType().loadFromDb(r))
                            .toArray(EntityTrainingType[]::new)
                )
                .onFailure().transform(e -> new RuntimeException("Error getTrainers", e))
                ;
    }

    /**
     *
     * @return training id
     */
    public Uni<Integer> add(Entity training) {
        return insert.execute(Tuple.of(training.time, training.trainer.getUserId(), training.trainingType.trainingType))
                .onItem().transform(rows -> rows.iterator().next().getInteger("training_id"))
                .onFailure().transform(e -> new RuntimeException("Error add", e))
                ;
    }

    public Uni<Void> update(Entity training) {
        return update.execute(Tuple.of(training.id, training.time, training.trainer.getUserId(), training.trainingType.trainingType))
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

    public static class Entity {
        private int id;
        @JsonFormat(pattern = Utils.DATE_TIME_FORMAT)
        private LocalDateTime time;
        private DbUser.EntityUser trainer;
        private EntityTrainingType trainingType;
        private String comment;

        public Entity loadFromDb(Row row) {
            this.id = row.getInteger("training_id");
            this.time = row.getLocalDateTime("training_time");
            this.trainer = new DbUser.EntityUser().loadFromDb(row);
            if (row.getString("training_type") != null) {
                this.trainingType = new EntityTrainingType().loadFromDb(row);
            }
            this.comment = row.getString("training_comment");
            return this;
        }
    }

    public static class EntityTrainingType {
        private String trainingType;
        private String trainingName;

        public EntityTrainingType loadFromDb(Row row) {
            this.trainingType = row.getString("training_type");
            this.trainingName = row.getString("name");
            return this;
        }

        public EntityTrainingType loadFromDb(JsonObject row) {
            this.trainingType = row.getString("training_type");
            this.trainingName = row.getString("name");
            return this;
        }

        public String getTrainingType() {
            return trainingType;
        }
    }
}
