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

@Path("/api")
@Authenticated
public class WebResourceSchedule {

    private static final Logger log = LoggerFactory.getLogger(WebResourceSchedule.class);

    @Inject
    DbCrudSchedule dbSchedule;

/*
    @GET
    @Path("{visitId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<DbCrudTrainer.Entity> get(@PathParam("visitId") int trainerId) {
        return dbTrainer.getById(trainerId);
    }
*/

    @GET
    @Path("schedule/list")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<DbCrudSchedule.Entity[]> list() {
        return dbSchedule.getAll();
    }

    @GET
    @Path("trainers/list")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<DbCrudSchedule.EntityTrainer[]> listTrainers() {
        return dbSchedule.getTrainers();
    }

    @GET
    @Path("trainers/trainingTypes")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<DbCrudSchedule.EntityTrainingType[]> listTrainingTypes() {
        return dbSchedule.getTrainingTypes();
    }

    @DELETE
    @Path("schedule/{scheduleId}")
    public Uni<Void> delete(@PathParam("scheduleId") int scheduleId) {
        return dbSchedule.delete(scheduleId);
    }

    @POST
    @Path("schedule")
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Integer> create(DbCrudSchedule.Entity schedule) {
        log.debug("Add visit {}", schedule);
        return dbSchedule.add(schedule);
    }

    @PUT
    @Path("schedule")
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Void> update(DbCrudSchedule.Entity schedule) {
        return dbSchedule.update(schedule);
    }
}
