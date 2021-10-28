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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.StreamSupport;

@Singleton
public class DbCrudTicket {
    private static final Logger log = LoggerFactory.getLogger(DbCrudTicket.class);

    @Inject
    PgPool client;

    private PreparedQuery<RowSet<Row>> selectTicketType;
    private PreparedQuery<RowSet<Row>> insertTicketType;
    private PreparedQuery<RowSet<Row>> updateTicketType;
    private PreparedQuery<RowSet<Row>> deleteTicketType;
    private PreparedQuery<RowSet<Row>> selectTicketsByUser;
    private PreparedQuery<RowSet<Row>> selectTicketById;
    private PreparedQuery<RowSet<Row>> insertTicket;
    private PreparedQuery<RowSet<Row>> updateTicket;
    private PreparedQuery<RowSet<Row>> deleteTicket;

    @PostConstruct
    void init() {
        selectTicketType = client.preparedQuery("SELECT t.*, array_agg(row_to_json(tt.*)) AS training_types_obj " +
                "FROM ticket_type t " +
                "JOIN training_type tt ON tt.training_type=ANY(t.training_types) " +
                "GROUP BY t.ticket_type_id");
        insertTicketType = client.preparedQuery("INSERT INTO ticket_type (training_types, ticket_name, ticket_cost, ticket_visits, ticket_days) VALUES ($1, $2, $3, $4, $5) RETURNING ticket_type_id");
        updateTicketType = client.preparedQuery("UPDATE ticket_type SET training_types=$2, ticket_name=$3, ticket_cost=$4, ticket_visits=$5, ticket_days=$6 WHERE ticket_type_id=$1");
        deleteTicketType = client.preparedQuery("DELETE FROM ticket_type WHERE ticket_type_id=$1");

        selectTicketsByUser = client.preparedQuery("SELECT * FROM training_ticket t " +
                "JOIN ticket_type tit ON tit.ticket_type_id=t.ticket_type_id " +
//                "JOIN (SELECT array_agg(row_to_json(tt.*)) AS training_types_obj FROM training_type tt WHERE tt.training_type=ANY(t.training_types)) training_type trt ON trt.training_type=" +
                "LEFT OUTER JOIN (SELECT ticket_id, COUNT(*) training_visit_count FROM training_visit GROUP BY ticket_id) vc ON t.ticket_id=vc.ticket_id " +
                "WHERE t.user_id=$1");
        selectTicketById = client.preparedQuery("SELECT * FROM training_ticket t " +
                "JOIN ticket_type tt ON tt.ticket_type_id=t.ticket_type_id " +
                "LEFT OUTER JOIN (SELECT ticket_id, COUNT(*) training_visit_count FROM training_visit WHERE ticket_id=$1 GROUP BY ticket_id) vc ON t.ticket_id=vc.ticket_id " +
                "WHERE t.ticket_id=$1");
        insertTicket = client.preparedQuery("INSERT INTO training_ticket (ticket_type_id, user_id, ticket_start, ticket_end) VALUES ($1, $2, $3, $4) RETURNING ticket_id");
        updateTicket = client.preparedQuery("UPDATE training_ticket SET ticket_type_id=$2, user_id=$3, ticket_start=$4, ticket_end=$5 WHERE ticket_id=$1");
        deleteTicket = client.preparedQuery("DELETE FROM training_ticket WHERE ticket_id=$1");
    }

    public Uni<EntityTicketType[]> getTicketTypes() {
        return selectTicketType
                .execute()
                .onItem().transform(set ->
                    StreamSupport.stream(set.spliterator(), false)
                            .map(r -> new EntityTicketType().loadFromDb(r))
                            .toArray(EntityTicketType[]::new)
                )
                .onFailure().transform(e -> new RuntimeException("Error getTicketTypes", e))
                ;
    }

    /**
     * @return ticketType id
     */
    public Uni<Integer> addTicketType(EntityTicketType ticketType) {
        return insertTicketType.execute(Tuple.of(ticketType.getTrainingTypeIds(), ticketType.name, ticketType.cost,
                        ticketType.visits, ticketType.days))
                .onItem().transform(rows -> rows.iterator().next().getInteger("ticket_type_id"))
                .onFailure().transform(e -> new RuntimeException("Error addTicketType", e))
                ;
    }

    public Uni<Void> updateTicketType(EntityTicketType ticketType) {
        return updateTicketType.execute(Tuple.of(ticketType.id, ticketType.getTrainingTypeIds(), ticketType.name,
                        ticketType.cost, ticketType.visits, ticketType.days))
                .onFailure().transform(e -> new RuntimeException("Error updateTicketType", e))
                .onItem().transform(u -> null)
                ;
    }

    public Uni<Void> deleteTicketType(int id) {
        return deleteTicketType.execute(Tuple.of(id))
                .onFailure().transform(e -> new RuntimeException("Error deleteTicketType", e))
                .onItem().transform(u -> null)
                ;
    }

    public Uni<EntityTicket[]> getTicketsByUser(int userId) {
        return selectTicketsByUser
                .execute(Tuple.of(userId))
                .onItem().transform(set ->
                    StreamSupport.stream(set.spliterator(), false)
                            .map(r -> new EntityTicket().loadFromDb(r))
                            .toArray(EntityTicket[]::new)
                )
                .onFailure().transform(e -> new RuntimeException("Error getTicketsByUser", e))
                ;
    }

    public Uni<EntityTicket> getTicketById(int ticketId) {
        return selectTicketById
                .execute(Tuple.of(ticketId))
                .onItem().transform(rows -> {
                    RowIterator<Row> rowIterator = rows.iterator();
                    if (rowIterator.hasNext()) {
                        log.debug("Ticket [{}] found", ticketId);
                        Row row = rowIterator.next();
                        return new EntityTicket().loadFromDb(row);
                    } else {
                        log.debug("Ticket [{}] not found", ticketId);
                        return null;
                    }
                })
                .onFailure().transform(e -> new RuntimeException("Error getTicketsByUser", e))
                ;
    }

    /**
     * @return ticketType id
     */
    public Uni<Integer> addTicket(EntityTicket ticket) {
        return insertTicket.execute(Tuple.of(ticket.ticketType.id, ticket.user.getUserId(), ticket.start, ticket.end))
                .onItem().transform(rows -> rows.iterator().next().getInteger("ticket_id"))
                .onFailure().transform(e -> new RuntimeException("Error addTicket", e))
                ;
    }

    public Uni<Void> updateTicket(EntityTicket ticket) {
        return updateTicket.execute(Tuple.of(ticket.id, ticket.ticketType.id, ticket.user.getUserId(), ticket.start, ticket.end))
                .onFailure().transform(e -> new RuntimeException("Error updateTicket", e))
                .onItem().transform(u -> null)
                ;
    }

    public Uni<Void> deleteTicket(int id) {
        return deleteTicket.execute(Tuple.of(id))
                .onFailure().transform(e -> new RuntimeException("Error deleteTicket", e))
                .onItem().transform(u -> null)
                ;
    }

    public static class EntityTicketType {
        private int id;
        private DbCrudTraining.EntityTrainingType[] trainingTypes;
        private String name;
        private int cost;
        private int visits;
        private int days;

        public EntityTicketType loadFromDb(Row row) {
            this.id = row.getInteger("ticket_type_id");
            this.name = row.getString("ticket_name");
            this.cost = row.getInteger("ticket_cost");
            this.cost = row.getInteger("ticket_cost");
            this.visits = row.getInteger("ticket_visits");
            this.days = row.getInteger("ticket_days");
            Object[] trainingTypesObjs = row.getArrayOfJsons("training_types_obj");
            this.trainingTypes = trainingTypesObjs == null ? null : Arrays.stream(trainingTypesObjs)
                    .map(tt -> new DbCrudTraining.EntityTrainingType().loadFromDb((JsonObject) tt))
                    .toArray(DbCrudTraining.EntityTrainingType[]::new);
            return this;
        }

        public String[] getTrainingTypeIds() {
            return Arrays.stream(trainingTypes).map(DbCrudTraining.EntityTrainingType::getTrainingType).toArray(String[]::new);
        }
    }

    /**
     * todo [!] add buy date
     */
    public static class EntityTicket {
        private int id;
        private EntityTicketType ticketType;
        private DbUser.EntityUser user;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        private LocalDateTime start;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        private LocalDateTime end;
        private int visited;

        public EntityTicket loadFromDb(Row row) {
            this.id = row.getInteger("ticket_id");
            this.ticketType = new EntityTicketType().loadFromDb(row);
//            this.user = row.getString("ticket_name");
            this.start = row.getLocalDateTime("ticket_start");
            this.end = row.getLocalDateTime("ticket_end");
            Integer visitedObj = row.getInteger("training_visit_count");
            this.visited = visitedObj != null ? visitedObj : 0;
            return this;
        }
    }
}
