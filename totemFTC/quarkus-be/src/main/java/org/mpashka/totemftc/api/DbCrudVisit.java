package org.mpashka.totemftc.api;

import io.smallrye.mutiny.Uni;
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
import java.util.Arrays;
import java.util.stream.StreamSupport;

import static java.util.Arrays.asList;

// todo [!] currently ticket_user_id == user_id which is wrong. To be fixed after implementing tickets
@Singleton
public class DbCrudVisit {
    private static final Logger log = LoggerFactory.getLogger(DbCrudVisit.class);

    @Inject
    PgPool client;

    private PreparedQuery<RowSet<Row>> selectByTraining;
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
                "WHERE v.training_id=$1 " +
                "ORDER BY u.nick_name");
        insert = client.preparedQuery("INSERT INTO training_visit (training_id, user_id, ticket_user_id, visit_comment, visit_mark_schedule, visit_mark_self, visit_mark_master) " +
                "VALUES ($1, $2, $3, $4, $5, $6, $7)");
        insertMarks = client.preparedQuery("INSERT INTO training_visit (training_id, user_id, ticket_user_id, visit_comment, " +
                "visit_mark_schedule, visit_mark_self, visit_mark_master) " +
                "VALUES ($1, $2, $3, $4, $5, $6, $7)");
        updateComment = client.preparedQuery("UPDATE training_visit SET visit_comment=$4 WHERE training_id=$1 AND user_id=$2 AND ticket_user_id=$3");
        updateSchedule = client.preparedQuery("UPDATE training_visit SET visit_mark_schedule=$4 WHERE training_id=$1 AND user_id=$2 AND ticket_user_id=$3");
        updateSelf = client.preparedQuery("UPDATE training_visit SET visit_mark_self=$4 WHERE training_id=$1 AND user_id=$2 AND ticket_user_id=$3");
        updateMaster = client.preparedQuery("UPDATE training_visit SET visit_mark_master=$4 WHERE training_id=$1 AND user_id=$2 AND ticket_user_id=$3");
        delete = client.preparedQuery("DELETE FROM training_visit WHERE training_id=$1 AND user_id=$2 AND ticket_user_id=$3");
    }

    public Uni<EntityVisit[]> getByTraining(int trainingId) {
        return selectByTraining
                .execute(Tuple.of(trainingId))
                .onItem().transform(set ->
                    StreamSupport.stream(set.spliterator(), false)
                            .map(r -> new EntityVisit().loadFromDb(r))
                            .toArray(EntityVisit[]::new)
                )
                .onFailure().transform(e -> new RuntimeException("Error getByTraining", e))
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
        return updateSchedule.execute(Tuple.of(entityVisit.trainingId, entityVisit.user.getUserId(), entityVisit.user.getUserId(), markSchedule))
                .onItem().transformToUni(updateResult ->
                        updateResult.rowCount() > 0 ? Uni.createFrom().<Void>item(null) :
                                insertMarks
                                        .execute(Tuple.from(asList(
                                                entityVisit.trainingId, entityVisit.user.getUserId(), entityVisit.user.getUserId(), entityVisit.comment,
                                                markSchedule, false, false)))
                                        .onItem().transform(u -> null)
                )
                .onFailure().transform(e -> new RuntimeException("Error update", e))
                ;
    }

    public Uni<Void> updateMarkSelf(EntityVisit entityVisit, boolean markSelf) {
        return updateSelf.execute(Tuple.of(entityVisit.trainingId, entityVisit.user.getUserId(), entityVisit.user.getUserId(), markSelf))
                .onItem().transformToUni(updateResult ->
                        updateResult.rowCount() > 0 ? Uni.createFrom().<Void>item(null) :
                                insertMarks
                                        .execute(Tuple.from(asList(
                                                entityVisit.trainingId, entityVisit.user.getUserId(), entityVisit.user.getUserId(), entityVisit.comment,
                                                false, markSelf, false)))
                                        .onItem().transform(u -> null)
                )
                .onFailure().transform(e -> new RuntimeException("Error update", e))
                ;
    }

    public Uni<Void> updateMarkMaster(EntityVisit entityVisit, boolean markMaster) {
        return updateMaster.execute(Tuple.of(entityVisit.trainingId, entityVisit.user.getUserId(), entityVisit.user.getUserId(), markMaster))
                .onItem().transformToUni(updateResult ->
                        updateResult.rowCount() > 0 ? Uni.createFrom().item((Void) null) :
                                insertMarks
                                        .execute(Tuple.from(asList(
                                                entityVisit.trainingId, entityVisit.user.getUserId(), entityVisit.user.getUserId(), entityVisit.comment,
                                                false, false, markMaster)))
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
        private int trainingId;
//        private int userId;
        private DbUser.EntityUser user;
        private String comment;
        private Integer ticketId;
        private boolean markSchedule;
        private boolean markSelf;
        private boolean markMaster;

        public EntityVisit loadFromDb(Row row) {
            this.trainingId = row.getInteger("training_id");
            this.user = new DbUser.EntityUser().loadFromDb(row);
            this.comment = row.getString("visit_comment");
            this.ticketId = row.getInteger("ticket_id");
            this.markSchedule = row.getBoolean("visit_mark_schedule");
            this.markSelf = row.getBoolean("visit_mark_self");
            this.markMaster = row.getBoolean("visit_mark_master");
            return this;
        }
    }
}
