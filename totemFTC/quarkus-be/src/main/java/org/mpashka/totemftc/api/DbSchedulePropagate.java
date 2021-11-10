package org.mpashka.totemftc.api;

import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.ScheduledExecution;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.PreparedQuery;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Load schedule from 'training_schedule', check if there is no
 * schedule in 'training' and copy appropriate lines from 'training_schedule' to 'training'
 */
@ApplicationScoped
public class DbSchedulePropagate {

    @Inject
    PgPool client;

    private PreparedQuery<RowSet<Row>> selectSchedule;
    private PreparedQuery<RowSet<Row>> selectTraining;
    private PreparedQuery<RowSet<Row>> insertTraining;

    @PostConstruct
    private void init() {
        selectSchedule = client.preparedQuery("SELECT * from training_schedule t");
        selectTraining = client.preparedQuery("SELECT * from training t WHERE training_time>$1 and training_time<$2");
        insertTraining = client.preparedQuery("INSERT INTO training (training_schedule_id, training_time, trainer, training_type) VALUES ($1, $2, $3, $4)");
    }

    @Scheduled(cron="0 30 0 * * ?")
    public void cronJob(ScheduledExecution execution) {
        schedulePropagate().await().atMost(Duration.of(10, ChronoUnit.MINUTES));
    }

    /**
     * Insert records into training from schedule for the next 7 days
     * if there are no records in trainings for that specific day.
     */
    public Uni<Void> schedulePropagate() {
        return selectSchedule.execute()
                .onItem().transform(set -> StreamSupport
                        .stream(set.spliterator(), false)
                        .map(r -> new EntitySchedule().loadFromDb(r))
                        .collect(Collectors.groupingBy(s -> s.time.getDayOfMonth()))
                )
                .onItem().transformToUni(schedule -> {
                    LocalDate now = LocalDate.now();
                    LocalDate nextWeek = now.plusDays(7);
                    return selectTraining.execute(Tuple.of(now, nextWeek))
                            .onItem().transform(set -> StreamSupport
                                    .stream(set.spliterator(), false)
                                    .map(r -> new EntityTraining().loadFromDb(r))
                                    .collect(Collectors.groupingBy(s -> s.time.toLocalDate())))
                            .onItem().transform(trainings -> new Entities(schedule, trainings));
                })
                .onItem().transformToUni(e -> {
                    List<Tuple> insertTrainingParams = new ArrayList<>();
                    for (int i = 0; i < 7; i++) {
                        LocalDate date = LocalDate.now().plusDays(i);
                        List<EntityTraining> trainings = e.trainings.get(date);
                        List<EntitySchedule> schedules = e.schedule.get(date.getDayOfWeek().getValue());
                        if (trainings == null && schedules != null) {
                            for (EntitySchedule schedule : schedules) {
                                insertTrainingParams.add(Tuple.of(schedule.id, LocalDateTime.of(date, schedule.time.toLocalTime()), schedule.trainerId, schedule.trainingType));
                            }
                        }
                    }
                    return insertTrainingParams.isEmpty() ? Uni.createFrom().item(null) :
                            insertTraining
                                    .executeBatch(insertTrainingParams)
                                    .onItem().transform(u -> null);
                });
    }

    private static class Entities {

        private Map<Integer, List<EntitySchedule>> schedule;
        private Map<LocalDate, List<EntityTraining>> trainings;

        public Entities(Map<Integer, List<EntitySchedule>> schedule, Map<LocalDate, List<EntityTraining>> trainings) {
            this.schedule = schedule;
            this.trainings = trainings;
        }
    }

    private static class EntitySchedule {
        private int id;
        private LocalDateTime time;
        private int trainerId;
        private String trainingType;

        public EntitySchedule loadFromDb(Row row) {
            this.id = row.getInteger("training_schedule_id");
            this.time = row.getLocalDateTime("training_time");
            this.trainerId = row.getInteger("trainer");
            this.trainingType = row.getString("training_type");
            return this;
        }
    }

    private static class EntityTraining {
        private int id;
        private Integer scheduleId;
        private LocalDateTime time;
        private int trainerId;
        private String trainingType;
        private String comment;

        public EntityTraining loadFromDb(Row row) {
            this.id = row.getInteger("training_id");
            this.scheduleId = row.getInteger("training_schedule_id");
            this.time = row.getLocalDateTime("training_time");
            this.trainerId = row.getInteger("trainer");
            this.trainingType = row.getString("training_type");
            this.comment = row.getString("comment");
            return this;
        }
    }


}
