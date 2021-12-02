package org.mpashka.totemftc.api;

import com.fasterxml.jackson.annotation.JsonFormat;
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
import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.StreamSupport;

@Singleton
public class DbCrudTraining {
    private static final Logger log = LoggerFactory.getLogger(DbCrudTraining.class);

    @Inject
    PgPool client;

    private PreparedQuery<RowSet<Row>> selectForUser;
    private PreparedQuery<RowSet<Row>> selectByDateForUser;
    private PreparedQuery<RowSet<Row>> selectByDateIntervalForUser;
    private PreparedQuery<RowSet<Row>> selectByDateIntervalForTrainer;
    private PreparedQuery<RowSet<Row>> selectTrainingTypes;
    private PreparedQuery<RowSet<Row>> insert;
    private PreparedQuery<RowSet<Row>> update;
    private PreparedQuery<RowSet<Row>> delete;

    @PostConstruct
    void init() {
        String sql = "SELECT * FROM training t, " +
                "   LATERAL (SELECT row_to_json(ut.*) trainer FROM user_info ut WHERE ut.user_id=t.trainer_id) ut, " +
                "   LATERAL (SELECT row_to_json(trt.*) training_type_obj FROM training_type trt WHERE trt.training_type=t.training_type) trt " +
                " <where> " +
                "ORDER BY t.training_time";
        selectForUser = client.preparedQuery(sql.replace("<where>", ""));
        selectByDateForUser = client.preparedQuery(sql.replace("<where>", "WHERE date(t.training_time) = $1"));
        selectByDateIntervalForUser = client.preparedQuery(sql.replace("<where>", "WHERE t.training_time >= $1 AND t.training_time <= $2 "));
        selectByDateIntervalForTrainer = client.preparedQuery("SELECT * FROM training t, " +
                "    LATERAL (SELECT row_to_json(tt.*) training_type_obj FROM training_type tt WHERE tt.training_type=t.training_type) trty " +
                "WHERE t.trainer_id = $1 " +
                "    AND t.training_time >= $2 AND t.training_time <= $3 " +
                "ORDER BY t.training_time"
        );
        selectTrainingTypes = client.preparedQuery("SELECT * FROM training_type");
        insert = client.preparedQuery("INSERT INTO training (training_time, trainer_id, training_type) VALUES ($1, $2, $3) RETURNING training_id");
        update = client.preparedQuery("UPDATE training SET training_time=$2, trainer_id=$3, training_type=$4 WHERE training_id=$1");
        delete = client.preparedQuery("DELETE FROM training WHERE training_id=$1");
    }

    public Uni<Entity[]> getAll() {
        return selectForUser
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
        return selectByDateForUser
                .execute(Tuple.of(date))
                .onItem().transform(set ->
                    StreamSupport.stream(set.spliterator(), false)
                            .map(r -> new Entity().loadFromDb(r))
                            .toArray(Entity[]::new)
                )
                .onFailure().transform(e -> new RuntimeException("Error getByDate", e))
                ;
    }

    public Uni<Entity[]> getByDateIntervalForUser(LocalDateTime from, LocalDateTime to) {
        return selectByDateIntervalForUser
                .execute(Tuple.of(from, to))
                .onItem().transform(set ->
                    StreamSupport.stream(set.spliterator(), false)
                            .map(r -> new Entity().loadFromDb(r))
                            .toArray(Entity[]::new)
                )
                .onFailure().transform(e -> new RuntimeException("Error getByDateInterval", e))
                ;
    }

    public Uni<Entity[]> getByDateIntervalForTrainer(int trainerId, LocalDateTime from, LocalDateTime to) {
        return selectByDateIntervalForTrainer
                .execute(Tuple.of(trainerId, from, to))
                .onItem().transform(set ->
                    StreamSupport.stream(set.spliterator(), false)
                            .map(r -> new Entity().loadFromDb(r))
                            .toArray(Entity[]::new)
                )
                .onFailure().transform(e -> new RuntimeException("Error getByDateInterval", e))
                ;
    }

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
            if (row.getColumnIndex("trainer") >=0) {
                JsonObject trainerJson = row.getJsonObject("trainer");
                if (trainerJson != null) {
                    this.trainer = new DbUser.EntityUser().loadFromDb(trainerJson);
                }
            }
            this.trainingType = new EntityTrainingType().loadFromDb(row.getJsonObject("training_type_obj"));
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
