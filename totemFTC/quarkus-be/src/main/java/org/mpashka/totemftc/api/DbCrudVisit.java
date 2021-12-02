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
    private PreparedQuery<RowSet<Row>> updateComment;
    private PreparedQuery<RowSet<Row>> updateMark;
    private PreparedQuery<RowSet<Row>> delete;

    @PostConstruct
    void init() {
        selectByTraining = client.preparedQuery(
                "SELECT * " +
                        "FROM training_visit v " +
                        "   JOIN user_info u ON v.user_id = u.user_id " +
                        "   LEFT OUTER JOIN training_ticket trt ON trt.ticket_id=v.ticket_id " +
                        "   LEFT JOIN ticket_type tit ON tit.ticket_type_id=trt.ticket_type_id " +
                        "WHERE v.training_id=$1 " +
                        "ORDER BY u.last_name, u.first_name, u.nick_name");

        selectByUser = client.preparedQuery(
                "SELECT * " +
                        "FROM training_visit v " +
                        "         LEFT OUTER JOIN training_ticket tti ON v.ticket_id = tti.ticket_id " +
                        "         LEFT OUTER JOIN ticket_type tit ON tit.ticket_type_id = tti.ticket_type_id, " +
                        "     LATERAL ( " +
                        "         SELECT * " +
                        "         FROM training t, " +
                        "              LATERAL (SELECT row_to_json(ut.*) trainer FROM user_info ut WHERE ut.user_id=t.trainer_id) ut, " +
                        "              LATERAL (SELECT row_to_json(trt.*) training_type_obj FROM training_type trt WHERE trt.training_type=t.training_type) trt " +
                        "         WHERE v.training_id = t.training_id) t " +
                        "WHERE v.user_id=$1 " +
                        "   AND t.training_time >= $2 " +
                        "ORDER BY t.training_time");
        selectByTicket = client.preparedQuery(
                "SELECT * " +
                        "FROM training_visit v, " +
                        "     LATERAL ( " +
                        "         SELECT * " +
                        "         FROM training t, " +
                        "              LATERAL (SELECT row_to_json(ut.*) trainer FROM user_info ut WHERE ut.user_id=t.trainer_id) ut, " +
                        "              LATERAL (SELECT row_to_json(trt.*) training_type_obj FROM training_type trt WHERE trt.training_type=t.training_type) trt " +
                        "         WHERE v.training_id = t.training_id) t " +
                        "WHERE v.ticket_id=$1 " +
                        "ORDER BY t.training_time");
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

    public Uni<EntityVisit[]> getByTicket(int ticketId) {
        return selectByTicket
                .execute(Tuple.of(ticketId))
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
//        log.debug("updateMarkSchedule Visit: {}, Training: {} ", entityVisit, entityVisit.training, );
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
        // todo [!] this is bad. either introduce new entity or use training
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
                this.user = new DbUser.EntityUser().loadFromDb(row);
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

        public EntityVisit loadFromDb(JsonObject visitJson, JsonObject userJson, JsonObject ticketJson, JsonObject ticketTypeJson) {
            this.trainingId = visitJson.getInteger("training_id");
            if (userJson != null) {
                this.user = new DbUser.EntityUser().loadFromDb(userJson);
            }
            this.comment = visitJson.getString("visit_comment");
            this.markSchedule = visitJson.getBoolean("visit_mark_schedule");
            this.markSelf = EntityVisitMark.valueOf(visitJson.getString("visit_mark_self"));
            this.markMaster = EntityVisitMark.valueOf(visitJson.getString("visit_mark_master"));
/*
            if (training) {
                this.training = new DbCrudTraining.Entity().loadFromDb(visitJson, false);
            }
*/
            if (ticketJson != null && visitJson.getInteger("ticket_id") != null) {
                this.ticket = new DbCrudTicket.EntityTicket().loadFromDb(ticketJson, ticketTypeJson);
            }
            return this;
        }
    }

    public enum EntityVisitMark {on, off, unmark};
}

