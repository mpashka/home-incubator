package org.mpashka.totemftc.api;

import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.ScheduledExecution;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.PreparedQuery;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Load schedule from 'training_schedule', check if there is no
 * schedule in 'training' and copy appropriate lines from 'training_schedule' to 'training'
 *
 * http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html
 */
@ApplicationScoped
public class DbScheduler {

    private static final Logger log = LoggerFactory.getLogger(DbScheduler.class);

    @Inject
    PgPool client;

    private PreparedQuery<RowSet<Row>> selectSchedule;
    private PreparedQuery<RowSet<Row>> selectTraining;
    private PreparedQuery<RowSet<Row>> insertTraining;

    @Inject
    WebSessionService webSessionService;

    @PostConstruct
    void init() {
        selectSchedule = client.preparedQuery("SELECT * from training_schedule t");
        selectTraining = client.preparedQuery("SELECT * from training t WHERE training_time>=$1 and training_time<$2");
        insertTraining = client.preparedQuery("INSERT INTO training (training_schedule_id, training_time, trainer_id, training_type) VALUES ($1, $2, $3, $4) RETURNING training_time, training_type");
    }

    @Scheduled(cron="0 30 0 * * ?")
    public void cronSchedulePropagate(ScheduledExecution execution) {
        schedulePropagate(LocalDate.now()).await().atMost(Duration.of(10, ChronoUnit.MINUTES));
    }

    @Scheduled(cron="0 0 1 * * ?")
    public void cronCleanupSessions(ScheduledExecution execution) {
        webSessionService.cleanupSessions().await().atMost(Duration.of(10, ChronoUnit.MINUTES));
    }

    /**
     * Insert records into training from schedule for the next 7 days
     * if there are no records in trainings for that specific day.
     */
    public Uni<RowSet<Row>> schedulePropagate(LocalDate weekStart) {
        log.debug("Propagate week schedule starting from {}", weekStart);
        return selectSchedule.execute()
                .onItem().transform(set -> StreamSupport
                        .stream(set.spliterator(), false)
                        .map(r -> new EntitySchedule().loadFromDb(r))
                        .collect(Collectors.groupingBy(s -> s.time.getDayOfMonth()))
                )
                .onItem().transformToUni(schedule -> {
                    LocalDateTime weekStartTime = LocalDateTime.of(weekStart, LocalTime.MIDNIGHT);
                    LocalDateTime weekEndTime = weekStartTime.plusDays(7);
                    return selectTraining.execute(Tuple.of(weekStartTime, weekEndTime))
                            .onItem().transform(set -> StreamSupport
                                    .stream(set.spliterator(), false)
                                    .map(r -> new EntityTraining().loadFromDb(r))
                                    .collect(Collectors.groupingBy(s -> s.time.toLocalDate())))
                            .onItem().transform(trainings -> new Entities(schedule, trainings));
                })
                .onItem().transformToUni(e -> {
                    List<Tuple> insertTrainingParams = new ArrayList<>();
                    for (int day = 0; day < 7; day++) {
                        LocalDate date = weekStart.plusDays(day);
                        List<EntityTraining> trainings = e.trainings.get(date);
                        List<EntitySchedule> schedules = e.schedule.get(date.getDayOfWeek().getValue());
                        log.debug("Day {}. Date:{}. Schedule: {}. Trainings: {}", day, date, schedules != null ? schedules.size() : -1, trainings != null ? trainings.size() : -1);
                        if (trainings == null && schedules != null) {
                            for (EntitySchedule schedule : schedules) {
                                log.debug("    {}", schedule);
                                insertTrainingParams.add(Tuple.of(schedule.id, LocalDateTime.of(date, schedule.time.toLocalTime()), schedule.trainerId, schedule.trainingType));
                            }
                        } else if (trainings != null && schedules != null) {
                            log.debug("    Day already has some schedule {}/{}. Schedule propagate aborted", date, trainings.size());
                        }
                    }
                    return insertTrainingParams.isEmpty() ? Uni.createFrom().item((RowSet<Row>) null) :
                            insertTraining.executeBatch(insertTrainingParams);
                })
                .onFailure().transform(e -> new RuntimeException("Error schedulePropagate", e));
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
            this.trainerId = row.getInteger("trainer_id");
            this.trainingType = row.getString("training_type");
            return this;
        }

        @Override
        public String toString() {
            return "EntitySchedule{" +
                    "id=" + id +
                    ", time=" + time +
                    ", trainerId=" + trainerId +
                    ", trainingType='" + trainingType + '\'' +
                    '}';
        }
    }

    /**
     * Simplified {@link DbCrudTraining.Entity} used just to propagate schedule
     */
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
            this.trainerId = row.getInteger("trainer_id");
            this.trainingType = row.getString("training_type");
            this.comment = row.getString("training_comment");
            return this;
        }
    }
}
