package org.mpashka.totemftc.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.smallrye.mutiny.Uni;
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
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Singleton
public class DbCrudFinance {
    private static final Logger log = LoggerFactory.getLogger(DbCrudFinance.class);

    @Inject
    PgPool client;

    private PreparedQuery<RowSet<Row>> selectIncomeForTrainer;
    private PreparedQuery<RowSet<Row>> selectIncome;
    private PreparedQuery<RowSet<Row>> selectTotalIncome;

    @PostConstruct
    void init() {
        selectIncomeForTrainer = client.preparedQuery("SELECT " +
                "    date, " +
                "    count(*) as trainings, " +
                "    sum(visits) as visits, " +
                "    coalesce(sum(ticket_income),0) as ticket_income, " +
                "    sum(income) as income " +
                "FROM ( " +
                "    SELECT date_trunc($1, training.training_time) as date, " +
                "        training.training_id, " +
                "        count(*) as visits, " +
                "        sum(ticket_type.ticket_cost / ticket_type.ticket_visits) as ticket_income, " +
                "        sum(coalesce(ticket_type.ticket_cost / ticket_type.ticket_visits, training_type.default_cost)) as income " +
                "    FROM training_visit " +
                "        LEFT OUTER JOIN training_ticket using (ticket_id) " +
                "        LEFT OUTER JOIN ticket_type using (ticket_type_id) " +
                "        LEFT JOIN training using (training_id) " +
                "        LEFT JOIN training_type using (training_type) " +
                "    WHERE " +
                "        training.trainer_id = $2 " +
                "        AND training.training_time >= $3 " +
                "        AND training.training_time <= $4 " +
                "    GROUP BY 1, training.training_id " +
                ") income " +
                "GROUP BY date " +
                "ORDER BY date ");

        //noinspection SqlAggregates
        selectIncome = client.preparedQuery("SELECT " +
                "    date, " +
                "    count(*) as trainings, " +
                "    sum(visits) as visits, " +
                "    coalesce(sum(ticket_income), 0) as ticket_income, " +
                "    sum(income) as income, " +
                "    user_info.* " +
                "FROM ( " +
                "    SELECT date_trunc($1, training.training_time) as date, " +
                "        training.trainer_id as trainer_id, " +
                "        training.training_id, " +
                "        count(*) as visits, " +
                "        sum(ticket_type.ticket_cost / ticket_type.ticket_visits) as ticket_income, " +
                "        sum(coalesce(ticket_type.ticket_cost / ticket_type.ticket_visits, training_type.default_cost)) as income " +
                "    FROM training_visit " +
                "        LEFT OUTER JOIN training_ticket using (ticket_id) " +
                "        LEFT OUTER JOIN ticket_type using (ticket_type_id) " +
                "        LEFT JOIN training using (training_id) " +
                "        LEFT JOIN training_type using (training_type) " +
                "    WHERE " +
                "        training.training_time >= $2 " +
                "        AND training.training_time <= $3 " +
                "    GROUP BY 1, training.trainer_id, training.training_id " +
                ") income " +
                "   LEFT JOIN user_info on user_info.user_id = income.trainer_id " +
                "GROUP BY date, user_info.user_id " +
                "ORDER BY date, user_info.first_name, user_info.last_name, user_info.nick_name ");

        selectTotalIncome = client.preparedQuery("SELECT " +
                "    date, " +
                "    count(*) as trainings, " +
                "    sum(visits) as visits, " +
                "    coalesce(sum(ticket_income), 0) as ticket_income, " +
                "    sum(income) as income " +
                "FROM ( " +
                "    SELECT date_trunc($1, training.training_time) as date, " +
                "        training.training_id, " +
                "        count(*) as visits, " +
                "        sum(ticket_type.ticket_cost / ticket_type.ticket_visits) as ticket_income, " +
                "        sum(coalesce(ticket_type.ticket_cost / ticket_type.ticket_visits, training_type.default_cost)) as income " +
                "    FROM training_visit " +
                "        LEFT OUTER JOIN training_ticket using (ticket_id) " +
                "        LEFT OUTER JOIN ticket_type using (ticket_type_id) " +
                "        LEFT JOIN training using (training_id) " +
                "        LEFT JOIN training_type using (training_type) " +
                "    WHERE " +
                "        training.training_time >= $2 " +
                "        AND training.training_time <= $3 " +
                "    GROUP BY 1, training.training_id " +
                ") income " +
                "GROUP BY date " +
                "ORDER BY date ");
    }

    public Uni<EntityIncome[]> getIncomeForTrainer(PeriodType periodType, int trainerId, LocalDate from, LocalDate to) {
        return selectIncomeForTrainer
                .execute(Tuple.of(periodType.name(), trainerId, LocalDateTime.of(from, LocalTime.MIDNIGHT), LocalDateTime.of(to, LocalTime.MIDNIGHT)))
                .onItem().transform(rows -> StreamSupport.stream(rows.spliterator(), false)
                        .map(row -> new EntityIncome().loadFromDb(row, false))
                        .toArray(EntityIncome[]::new))
                .onFailure().transform(e -> new RuntimeException("Error getIncomeForTrainer", e))
                ;
    }

    public Uni<EntityIncome[]> getTrainerIncome(PeriodType periodType, LocalDate from, LocalDate to) {
        return selectIncome
                .execute(Tuple.of(periodType.name(), LocalDateTime.of(from, LocalTime.MIDNIGHT), LocalDateTime.of(to, LocalTime.MIDNIGHT)))
                .onItem().transform(rows -> StreamSupport.stream(rows.spliterator(), false)
                        .map(row -> new EntityIncome().loadFromDb(row, true))
                        .toArray(EntityIncome[]::new))
                .onFailure().transform(e -> new RuntimeException("Error getIncome", e))
                ;
    }

    public Uni<EntityIncome[]> getTotalIncome(PeriodType periodType, LocalDate from, LocalDate to) {
        return selectTotalIncome
                .execute(Tuple.of(periodType.name(), LocalDateTime.of(from, LocalTime.MIDNIGHT), LocalDateTime.of(to, LocalTime.MIDNIGHT)))
                .onItem().transform(rows -> StreamSupport.stream(rows.spliterator(), false)
                        .map(row -> new EntityIncome().loadFromDb(row, false))
                        .toArray(EntityIncome[]::new))
                .onFailure().transform(e -> new RuntimeException("Error getIncome", e))
                ;
    }


    public static class EntityIncome {
        @JsonFormat(pattern = Utils.DATE_FORMAT)
        private LocalDate date;
        private int trainings;
        private int visits;
        private double ticketIncome;
        private double income;
        private DbUser.EntityUser trainer;

        public EntityIncome loadFromDb(Row row, boolean loadTrainer) {
            this.date = row.getLocalDate("date");
            this.trainings = row.getInteger("trainings");
            this.visits = row.getInteger("visits");
            this.ticketIncome = row.getDouble("ticket_income");
            this.income = row.getDouble("income");
            if (loadTrainer) {
                trainer = new DbUser.EntityUser().loadFromDb(row);
            }
            return this;
        }

    }

    public enum PeriodType {
        month, week
    }
}
