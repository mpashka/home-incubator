package org.mpashka.totemftc.api;

import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 */
@Path("/api/visit")
@Authenticated
public class WebResourceVisit {

    private static final Logger log = LoggerFactory.getLogger(WebResourceVisit.class);

    @Inject
    DbCrudVisit dbVisit;

    @GET
    @Path("byTraining/{trainingId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<DbCrudVisit.EntityVisit[]> list(@PathParam("trainingId") int trainingId) {
        return dbVisit.getByTraining(trainingId);
    }

    @PUT
    @Path("delete")
    public Uni<Void> delete(DbCrudVisit.EntityVisit visit) {
        return dbVisit.delete(visit);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Integer> create(DbCrudVisit.EntityVisit visit) {
        log.debug("Add visit {}", visit);
        return dbVisit.add(visit);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Void> update(DbCrudVisit.EntityVisit visit) {
        return dbVisit.updateComment(visit);
    }

    @PUT
    @Path("/markSchedule/{markSchedule}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Void> updateMarkSchedule(DbCrudVisit.EntityVisit visit, @PathParam("markSchedule") boolean markSchedule) {
        return dbVisit.updateMarkSchedule(visit, markSchedule);
    }

    @PUT
    @Path("/markSelf/{markSelf}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Void> updateMarkSelf(DbCrudVisit.EntityVisit visit, @PathParam("markSelf") boolean markSelf) {
        return dbVisit.updateMarkSelf(visit, markSelf);
    }

    @PUT
    @Path("/markMaster/{markMaster}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Void> updateMarkMaster(DbCrudVisit.EntityVisit visit, @PathParam("markMaster") boolean markMaster) {
        return dbVisit.updateMarkMaster(visit, markMaster);
    }
}
