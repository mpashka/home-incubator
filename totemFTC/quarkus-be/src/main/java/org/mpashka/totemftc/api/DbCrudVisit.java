package org.mpashka.totemftc.api;

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
import java.time.LocalDateTime;
import java.util.stream.StreamSupport;

// todo [!] currently ticket_user_id == user_id which is wrong. To be fixed after implementing tickets
@Singleton
public class DbCrudVisit {
    private static final Logger log = LoggerFactory.getLogger(DbCrudVisit.class);

    @Inject
    PgPool client;

    private PreparedQuery<RowSet<Row>> selectByTraining;
    private PreparedQuery<RowSet<Row>> selectByUser;
    private PreparedQuery<RowSet<Row>> selectByTicket;
    private PreparedQuery<RowSet<Row>> selectForUserByTicket;
    private PreparedQuery<RowSet<Row>> updateComment;
    private PreparedQuery<RowSet<Row>> updateMark;
    private PreparedQuery<RowSet<Row>> delete;

    @PostConstruct
    void init() {
        selectByTraining = client.preparedQuery(
                "SELECT * " +
                        "FROM visit_view " +
                        "WHERE training_id=$1 " +
                        "ORDER BY user_last_name, user_first_name, user_nick_name");

        selectByUser = client.preparedQuery(
                "SELECT * " +
                        "FROM visit_view " +
                        "WHERE user_id=$1 " +
                        "   AND training_time >= $2 " +
                        "ORDER BY training_time");
        selectForUserByTicket = client.preparedQuery(
                "SELECT * " +
                        "FROM visit_view " +
                        "WHERE user_id=$1 AND ticket_id=$2 " +
                        "ORDER BY training_time");
        selectByTicket = client.preparedQuery(
                "SELECT * " +
                        "FROM visit_view " +
                        "WHERE ticket_id=$1 " +
                        "ORDER BY training_time");
        updateMark = client.preparedQuery("SELECT * FROM mark_visit($1, $2, $3, $4, $5::mark_type_enum, $6::mark_type_enum, $7, $8::mark_type_enum, $9::mark_type_enum, $10)");
        updateComment = client.preparedQuery("UPDATE training_visit SET visit_comment=$4 WHERE training_id=$1 AND user_id=$2 AND ticket_user_id=$3");
        delete = client.preparedQuery("SELECT * FROM delete_visit($1, $2)");
    }

    public Uni<EntityVisit[]> getByTraining(int trainingId) {
        return selectByTraining
                .execute(Tuple.of(trainingId))
                .onItem().transform(set ->
                    StreamSupport.stream(set.spliterator(), false)
                            .map(r -> new EntityVisit().loadFromDb(r, true, false, true))
                            .toArray(EntityVisit[]::new)
                )
                .onFailure().transform(e -> new RuntimeException("Error getByTraining", e))
                ;
    }

    public Uni<EntityVisit[]> getByUser(int userId, LocalDateTime from) {
        return selectByUser
                .execute(Tuple.of(userId, from))
                .onItem().transform(set ->
                    StreamSupport.stream(set.spliterator(), false)
                            .map(r -> new EntityVisit().loadFromDb(r, false, true, true))
                            .toArray(EntityVisit[]::new)
                )
                .onFailure().transform(e -> new RuntimeException("Error getByTraining", e))
                ;
    }

    public Uni<EntityVisit[]> getByTicket(int userId, int ticketId) {
        return (userId == -1 ? selectByTicket.execute(Tuple.of(ticketId)) : selectForUserByTicket.execute(Tuple.of(userId, ticketId)))
                .onItem().transform(set ->
                    StreamSupport.stream(set.spliterator(), false)
                            .map(r -> new EntityVisit().loadFromDb(r, false, true, false))
                            .toArray(EntityVisit[]::new)
                )
                .onFailure().transform(e -> new RuntimeException("Error getByTicket", e))
                ;
    }

    /** Update comment */
    public Uni<Void> updateComment(EntityVisit entityVisit) {
        return updateComment.execute(Tuple.of(entityVisit.trainingId, entityVisit.user.getUserId(), entityVisit.user.getUserId(), entityVisit.comment))
                .onFailure().transform(e -> new RuntimeException("Error update", e))
                .onItem().transform(u -> null)
                ;
    }

    public Uni<DbCrudTicket.EntityTicket> updateMark(EntityVisit entityVisit, Boolean markSchedule, EntityVisitMark markSelf, EntityVisitMark markMaster) {
//        log.debug("updateMarkSchedule Visit: {}, Training: {} ", entityVisit, entityVisit.training);
        return updateMark.execute(Tuple.from(new Object[]{
                        entityVisit.trainingId, entityVisit.user.getUserId(), entityVisit.user.getUserId(),
                        markSchedule, markSelf, markMaster,
                        entityVisit.markSchedule, entityVisit.markSelf, entityVisit.markMaster,
                        entityVisit.comment
                }))
                .onFailure().transform(e -> new RuntimeException("Error update", e))
                .onItem().transform(this::loadTicketIfPresent);
    }

    public Uni<DbCrudTicket.EntityTicket> delete(DbCrudVisit.EntityVisit entityVisit) {
        return delete.execute(Tuple.of(entityVisit.trainingId, entityVisit.user.getUserId()))
                .onFailure().transform(e -> new RuntimeException("Error delete", e))
                .onItem().transform(this::loadTicketIfPresent);
    }

    private DbCrudTicket.EntityTicket loadTicketIfPresent(RowSet<Row> rowSet) {
        Row row = rowSet.iterator().next();
        return row.getInteger("ticket_id") != null ? new DbCrudTicket.EntityTicket().loadFromDb(row) : null;
    }

    public static class EntityVisit {
        // todo [!] this is bad. either introduce new entity or use training. optimization can be done on client side by removing unused objects
        private int trainingId;
//        private int userId;
        private DbUser.EntityUser user;
        private String comment;
//        private Integer ticketId;
        private boolean markSchedule;
        private EntityVisitMark markSelf;
        private EntityVisitMark markMaster;
        private DbCrudTraining.Entity training;
        private DbCrudTicket.EntityTicket ticket;


        public EntityVisit loadFromDb(Row row, boolean user, boolean training, boolean ticket) {
            this.trainingId = row.getInteger("training_id");
            if (user) {
                JsonObject userJson = row.getJsonObject("user_info");
                this.user = new DbUser.EntityUser().loadFromDb(userJson);
            }
            this.comment = row.getString("visit_comment");
            this.markSchedule = row.getBoolean("visit_mark_schedule");
            this.markSelf = EntityVisitMark.valueOf(row.getString("visit_mark_self"));
            this.markMaster = EntityVisitMark.valueOf(row.getString("visit_mark_master"));
            if (training) {
                this.training = new DbCrudTraining.Entity().loadFromDb(row);
            }
            if (ticket && row.getInteger("ticket_id") != null) {
                this.ticket = new DbCrudTicket.EntityTicket().loadFromDb(row);
            }
            return this;
        }
    }

    public enum EntityVisitMark {on, off, unmark};
}

