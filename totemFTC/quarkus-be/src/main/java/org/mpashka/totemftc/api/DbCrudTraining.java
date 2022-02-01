package org.mpashka.totemftc.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.PreparedQuery;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowIterator;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.StreamSupport;

@Singleton
public class DbCrudTraining {
    private static final Logger log = LoggerFactory.getLogger(DbCrudTraining.class);

    @Inject
    PgPool client;

    private PreparedQuery<RowSet<Row>> selectForUser;
    private PreparedQuery<RowSet<Row>> selectById;
    private PreparedQuery<RowSet<Row>> selectByDateForUser;
    private PreparedQuery<RowSet<Row>> selectByDateIntervalForUser;
    private PreparedQuery<RowSet<Row>> selectByDateIntervalForTrainer;
    private PreparedQuery<RowSet<Row>> selectTrainingTypes;
    private PreparedQuery<RowSet<Row>> insertTrainingType;
    private PreparedQuery<RowSet<Row>> updateTrainingType;
    private PreparedQuery<RowSet<Row>> deleteTrainingType;
    private PreparedQuery<RowSet<Row>> insert;
    private PreparedQuery<RowSet<Row>> updateForAdmin;
    private PreparedQuery<RowSet<Row>> updateForTrainer;
    private PreparedQuery<RowSet<Row>> updateCommentForAdmin;
    private PreparedQuery<RowSet<Row>> updateCommentForTrainer;
    private PreparedQuery<RowSet<Row>> deleteForAdmin;
    private PreparedQuery<RowSet<Row>> deleteForTrainer;

    @PostConstruct
    void init() {
        selectById = client.preparedQuery("SELECT * FROM training_view WHERE training_id=$1");
        selectForUser = client.preparedQuery("SELECT * FROM training_view ORDER BY training_time");
        selectByDateForUser = client.preparedQuery("SELECT * FROM training_view " +
                "WHERE date(training_time) = $1 " +
                "ORDER BY training_time");
        selectByDateIntervalForUser = client.preparedQuery("SELECT * FROM training_view " +
                "WHERE training_time >= $1 AND training_time <= $2 " +
                "ORDER BY training_time");
        selectByDateIntervalForTrainer = client.preparedQuery("SELECT * FROM training_view " +
                "WHERE trainer_id = $1 " +
                "    AND training_time >= $2 AND training_time <= $3 " +
                "ORDER BY training_time"
        );
        selectTrainingTypes = client.preparedQuery("SELECT * FROM training_type ORDER BY training_type");
        insertTrainingType = client.preparedQuery("INSERT INTO training_type (training_type, name, default_cost) VALUES ($1, $2, $3)");
        updateTrainingType = client.preparedQuery("UPDATE training_type SET name=$2, default_cost=$3 WHERE training_type=$1");
        deleteTrainingType = client.preparedQuery("DELETE FROM training_type WHERE training_type=$1");
        insert = client.preparedQuery("INSERT INTO training (training_time, trainer_id, training_type) VALUES ($1, $2, $3) RETURNING training_id");
        updateForAdmin = client.preparedQuery("UPDATE training SET trainer_id=$2, training_time=$3, training_type=$4 WHERE training_id=$1");
        updateCommentForAdmin = client.preparedQuery("UPDATE training SET training_comment=$3 WHERE training_id=$1 AND trainer_id=$2");
        updateCommentForTrainer = client.preparedQuery("UPDATE training SET training_comment=$2 WHERE training_id=$1");
        updateForTrainer = client.preparedQuery("UPDATE training SET training_time=$3, training_type=$4 WHERE training_id=$1 AND trainer_id=$2");
        deleteForAdmin = client.preparedQuery("DELETE FROM training WHERE training_id=$1");
        deleteForTrainer = client.preparedQuery("DELETE FROM training WHERE training_id=$1 AND trainer_id=$2");
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

    public Uni<Entity> getById(int trainingId) {
        return selectById
                .execute(Tuple.of(trainingId))
                .onItem().transform(rows -> {
                    RowIterator<Row> rowIterator = rows.iterator();
                    if (rowIterator.hasNext()) {
                        log.debug("Training [{}] found", trainingId);
                        Row row = rowIterator.next();
                        return new Entity().loadFromDb(row);
                    } else {
                        log.debug("Training [{}] not found", trainingId);
                        return null;
                    }
                })
                .onFailure().transform(e -> new RuntimeException("Error getAll", e));
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

    public Uni<Void> addTrainingType(EntityTrainingType trainingType) {
        return insertTrainingType.execute(Tuple.of(trainingType.trainingType, trainingType.trainingName, trainingType.defaultCost))
                .onItem().transform(u -> (Void) null)
                .onFailure().transform(e -> new RuntimeException("Error addTrainingType", e))
                ;
    }

    public Uni<Void> updateTrainingType(EntityTrainingType trainingType) {
        return updateTrainingType.execute(Tuple.of(trainingType.trainingType, trainingType.trainingName, trainingType.defaultCost))
                .onItem().transform(u -> (Void) null)
                .onFailure().transform(e -> new RuntimeException("Error updateTrainingType", e))
                ;
    }

    public Uni<Void> deleteTrainingType(String trainingType) {
        return deleteTrainingType.execute(Tuple.of(trainingType))
                .onItem().transform(u -> (Void) null)
                .onFailure().transform(e -> new RuntimeException("Error deleteTrainingType", e))
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

    public Uni<Void> update(Entity training, boolean isAdmin) {
        PreparedQuery<RowSet<Row>> update = isAdmin ?  updateForAdmin : updateForTrainer;
        return update.execute(Tuple.of(training.id, training.trainer.getUserId(), training.time, training.trainingType.trainingType))
                .onFailure().transform(e -> new RuntimeException("Error update", e))
                .onItem().transform(u -> null)
                ;
    }

    public Uni<Void> updateComment(Entity training, boolean isAdmin) {
        Uni<RowSet<Row>> execute;
        if (isAdmin) {
            execute = updateCommentForAdmin.execute(Tuple.of(training.id, training.comment));
        } else {
            execute = updateCommentForTrainer.execute(Tuple.of(training.id, training.trainer.getUserId(), training.comment));
        }
        return execute
                .onFailure().transform(e -> new RuntimeException("Error update", e))
                .onItem().transform(u -> null)
                ;
    }

    public Uni<Void> delete(int id) {
        return deleteForAdmin.execute(Tuple.of(id))
                .onFailure().transform(e -> new RuntimeException("Error delete", e))
                .onItem().transform(u -> null)
                ;
    }

    public Uni<Void> delete(int id, int trainerId) {
        return deleteForTrainer.execute(Tuple.of(id, trainerId))
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

        public DbUser.EntityUser getTrainer() {
            return trainer;
        }

        public Entity loadFromDb(Row row) {
            this.id = row.getInteger("training_id");
            this.time = row.getLocalDateTime("training_time");
            if (row.getColumnIndex("trainer") >=0) {
                JsonObject trainerJson = row.getJsonObject("trainer");
                if (trainerJson != null) {
                    this.trainer = new DbUser.EntityUser().loadFromDb(trainerJson);
                }
            }
            this.trainingType = new EntityTrainingType().loadFromDb(row);
            this.comment = row.getString("training_comment");
            return this;
        }
    }

    public static class EntityTrainingType {
        private String trainingType;
        private String trainingName;
        private double defaultCost;

        public EntityTrainingType loadFromDb(Row row) {
            this.trainingType = row.getString("training_type");
            this.trainingName = row.getString("training_name");
            this.defaultCost = row.getDouble("default_cost");
            return this;
        }

        public EntityTrainingType loadFromDb(JsonObject row) {
            this.trainingType = row.getString("training_type");
            this.trainingName = row.getString("training_name");
            this.defaultCost = row.getDouble("default_cost");
            return this;
        }

        public String getTrainingType() {
            return trainingType;
        }
    }
}
