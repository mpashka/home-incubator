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

import static java.util.Arrays.asList;

// todo [!] currently ticket_user_id == user_id which is wrong. To be fixed after implementing tickets
@Singleton
public class DbCrudVisit {
    private static final Logger log = LoggerFactory.getLogger(DbCrudVisit.class);

    @Inject
    PgPool client;

    private PreparedQuery<RowSet<Row>> selectByTraining;
    private PreparedQuery<RowSet<Row>> selectByUser;
    private PreparedQuery<RowSet<Row>> selectByTicket;
    private PreparedQuery<RowSet<Row>> insert;
    private PreparedQuery<RowSet<Row>> insertMarks;
    private PreparedQuery<RowSet<Row>> updateComment;
    private PreparedQuery<RowSet<Row>> updateSchedule;
    private PreparedQuery<RowSet<Row>> updateSelf;
    private PreparedQuery<RowSet<Row>> updateMaster;
    private PreparedQuery<RowSet<Row>> delete;

    @PostConstruct
    void init() {
        selectByTraining = client.preparedQuery("SELECT * FROM training_visit v " +
                "JOIN user_info u ON v.user_id = u.user_id " +
                "LEFT OUTER JOIN training_ticket trt ON trt.ticket_id=v.ticket_id " +
                "LEFT JOIN ticket_type tit ON tit.ticket_type_id=trt.ticket_type_id " +
                // todo check optimization WHERE tv.ticket_id=v.ticket_id
                "LEFT OUTER JOIN (SELECT ticket_id, COUNT(*) training_visit_count FROM training_visit tv WHERE tv.ticket_id=v.ticket_id GROUP BY ticket_id) vc ON v.ticket_id=vc.ticket_id " +
                "WHERE v.training_id=$1 " +
                "ORDER BY u.last_name, u.first_name, u.nick_name");

        selectByUser = client.preparedQuery(
                "SELECT * " +
                        "FROM training_visit v " +
                        "         LEFT OUTER JOIN training_ticket tti ON v.ticket_id = tti.ticket_id " +
                        "         LEFT OUTER JOIN ticket_type tit ON tit.ticket_type_id = tti.ticket_type_id " +
                        "         LEFT OUTER JOIN (SELECT ticket_id, COUNT(*) training_visit_count FROM training_visit GROUP BY ticket_id) vc ON tti.ticket_id=vc.ticket_id, " +
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
                        "   (SELECT COUNT(*) training_visit_count FROM training_visit WHERE ticket_id=$1) tvc, " +
                        "     LATERAL ( " +
                        "         SELECT * " +
                        "         FROM training t, " +
                        "              LATERAL (SELECT row_to_json(ut.*) trainer FROM user_info ut WHERE ut.user_id=t.trainer_id) ut, " +
                        "              LATERAL (SELECT row_to_json(trt.*) training_type_obj FROM training_type trt WHERE trt.training_type=t.training_type) trt " +
                        "         WHERE v.training_id = t.training_id) t " +
                        "WHERE v.ticket_id=$1 " +
                        "ORDER BY t.training_time");
        insert = client.preparedQuery("INSERT INTO training_visit (training_id, user_id, ticket_user_id, visit_comment, visit_mark_schedule, visit_mark_self, visit_mark_master) " +
                "VALUES ($1, $2, $3, $4, $5, $6::mark_type_enum, $7::mark_type_enum)");
        insertMarks = client.preparedQuery("INSERT INTO training_visit (training_id, user_id, ticket_user_id, visit_comment, " +
                "visit_mark_schedule, visit_mark_self, visit_mark_master) " +
                "VALUES ($1, $2, $3, $4, $5, $6::mark_type_enum, $7::mark_type_enum)");
        updateComment = client.preparedQuery("UPDATE training_visit SET visit_comment=$4 WHERE training_id=$1 AND user_id=$2 AND ticket_user_id=$3");
        updateSchedule = client.preparedQuery("UPDATE training_visit SET visit_mark_schedule=$4 WHERE training_id=$1 AND user_id=$2 AND ticket_user_id=$3");
        updateSelf = client.preparedQuery("UPDATE training_visit SET visit_mark_self=$4::mark_type_enum WHERE training_id=$1 AND user_id=$2 AND ticket_user_id=$3");
        updateMaster = client.preparedQuery("UPDATE training_visit SET visit_mark_master=$4::mark_type_enum WHERE training_id=$1 AND user_id=$2 AND ticket_user_id=$3");
        delete = client.preparedQuery("DELETE FROM training_visit WHERE training_id=$1 AND user_id=$2 AND ticket_user_id=$3");
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

    /**
     *
     * @return trainer id
     */
    public Uni<Void> add(EntityVisit entityVisit) {
        return insert.execute(Tuple.from(asList(entityVisit.trainingId, entityVisit.user.getUserId(), entityVisit.user.getUserId(), entityVisit.comment,
                        entityVisit.markSchedule, entityVisit.markSelf, entityVisit.markMaster)))
                .onFailure().transform(e -> new RuntimeException("Error add", e))
                .onItem().transform(u -> null)
                ;
    }

    /** Update comment */
    public Uni<Void> updateComment(EntityVisit entityVisit) {
        return updateComment.execute(Tuple.of(entityVisit.trainingId, entityVisit.user.getUserId(), entityVisit.user.getUserId(), entityVisit.comment))
                .onFailure().transform(e -> new RuntimeException("Error update", e))
                .onItem().transform(u -> null)
                ;
    }

    public Uni<Void> updateMarkSchedule(EntityVisit entityVisit, boolean markSchedule) {
//        log.debug("updateMarkSchedule Visit: {}, Training: {} ", entityVisit, entityVisit.training, );
        return updateSchedule.execute(Tuple.of(entityVisit.trainingId, entityVisit.user.getUserId(), entityVisit.user.getUserId(), markSchedule))
                .onItem().transformToUni(updateResult ->
                        updateResult.rowCount() > 0 ? Uni.createFrom().voidItem() :
                                insertMarks
                                        .execute(Tuple.from(asList(
                                                entityVisit.trainingId, entityVisit.user.getUserId(), entityVisit.user.getUserId(), entityVisit.comment,
                                                markSchedule, EntityVisitMark.unmark.name(), EntityVisitMark.unmark.name())))
                                        .onItem().transform(u -> null)
                )
                .onFailure().transform(e -> new RuntimeException("Error update", e))
                ;
    }

    public Uni<Void> updateMarkSelf(EntityVisit entityVisit, EntityVisitMark markSelf) {
        return updateSelf.execute(Tuple.of(entityVisit.trainingId, entityVisit.user.getUserId(), entityVisit.user.getUserId(), markSelf.name()))
                .onItem().invoke(updateResult -> log.info("Mark self {} {} row count {}", entityVisit, markSelf, updateResult.rowCount()))
                .onItem().transformToUni(updateResult ->
                        updateResult.rowCount() > 0 ? Uni.createFrom().voidItem() :
                                insertMarks
                                        .execute(Tuple.from(asList(
                                                entityVisit.trainingId, entityVisit.user.getUserId(), entityVisit.user.getUserId(), entityVisit.comment,
                                                false, markSelf.name(), EntityVisitMark.unmark.name())))
                                        .onItem().invoke(insertResult -> log.info("Mark self {} {} insert row count {}", entityVisit, markSelf, insertResult.rowCount()))
                                        .onItem().transform(u -> null)
                )
                .onFailure().transform(e -> new RuntimeException("Error update", e))
                ;
    }

    public Uni<Void> updateMarkMaster(EntityVisit entityVisit, EntityVisitMark markMaster) {
        return updateMaster.execute(Tuple.of(entityVisit.trainingId, entityVisit.user.getUserId(), entityVisit.user.getUserId(), markMaster))
                .onItem().transformToUni(updateResult ->
                        updateResult.rowCount() > 0 ? Uni.createFrom().voidItem() :
                                insertMarks
                                        .execute(Tuple.from(asList(
                                                entityVisit.trainingId, entityVisit.user.getUserId(), entityVisit.user.getUserId(), entityVisit.comment,
                                                false, EntityVisitMark.unmark.name(), markMaster.name())))
                                        .onItem().transform(u -> null)
                )
                .onFailure().transform(e -> new RuntimeException("Error update", e))
                ;
    }

    public Uni<Void> delete(DbCrudVisit.EntityVisit entityVisit) {
        return delete.execute(Tuple.of(entityVisit.trainingId, entityVisit.user.getUserId(), entityVisit.user.getUserId()))
                .onFailure().transform(e -> new RuntimeException("Error delete", e))
                .onItem().transform(u -> null)
                ;
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

