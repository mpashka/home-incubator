package org.mpashka.totemftc.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.PreparedQuery;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private PreparedQuery<RowSet<Row>> selectTrainingTypes;
    private PreparedQuery<RowSet<Row>> selectTrainers;
    private PreparedQuery<RowSet<Row>> insert;
    private PreparedQuery<RowSet<Row>> update;
    private PreparedQuery<RowSet<Row>> delete;

    void onStart(@Observes StartupEvent ev) {
        select = client.preparedQuery("SELECT * from training t " +
                "JOIN user_info u ON t.trainer = u.user_id " +
                "JOIN training_type tt on t.training_type = tt.training_type " +
                "ORDER BY t.training_time");
        selectByDate = client.preparedQuery("SELECT * from training t " +
                "JOIN user_info u ON t.trainer = u.user_id " +
                "JOIN training_type tt on t.training_type = tt.training_type " +
                "WHERE date(t.training_time) = $1 " +
                "ORDER BY t.training_time");
        selectTrainingTypes = client.preparedQuery("SELECT * from training_type");
        selectTrainers = client.preparedQuery("SELECT * from trainer_type tt JOIN user_info u on u.user_id = tt.user_id");
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

    public Uni<EntityTrainer[]> getTrainers() {
        return selectTrainers
                .execute()
                .onItem().transform(set -> StreamSupport.stream(set.spliterator(), false)
                            .map(r -> new EntityTrainer().loadFromDb(r))
                            .collect(Collectors.groupingBy(DbUser.EntityUser::getUserId))
                            .values().stream().map(l -> {
                                EntityTrainer t = l.get(0);
                                t.trainingTypes = l.stream().map(t0 -> t0.trainingTypes[0]).toArray(String[]::new);
                                return t;
                            })
                            .toArray(EntityTrainer[]::new)
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
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        private LocalDateTime time;
//        private int trainerId;
        private DbUser.EntityUser trainer;
        private EntityTrainingType trainingType;
        private String comment;

        public Entity loadFromDb(Row row) {
            this.id = row.getInteger("training_id");
            this.time = row.getLocalDateTime("training_time");
//            this.trainerId = row.getInteger("trainer");
            this.trainer = new DbUser.EntityUser().loadFromDb(row);
            this.trainingType = new EntityTrainingType().loadFromDb(row);
            this.comment = row.getString("comment");
            return this;
        }
    }

    public static class EntityTrainer extends DbUser.EntityUser {
        private String[] trainingTypes;

        public EntityTrainer loadFromDb(Row row) {
            super.loadFromDb(row);
            this.trainingTypes = new String[]{row.getString("training_type")};
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

        public String getTrainingType() {
            return trainingType;
        }
    }
}