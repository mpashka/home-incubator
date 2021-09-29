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

@Path("/api/trainer")
@Authenticated
public class WebResourceTrainer {

    private static final Logger log = LoggerFactory.getLogger(WebResourceTrainer.class);

    @Inject
    DbCrudTrainer dbTrainer;

    @GET
    @Path("{trainerId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<DbCrudTrainer.EntityTrainer> get(@PathParam("trainerId") int trainerId) {
        return dbTrainer.getById(trainerId);
    }

    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<DbCrudTrainer.EntityTrainer[]> list() {
        return dbTrainer.getAll();
    }

    @DELETE
    @Path("{trainerId}")
    public Uni<Void> delete(@PathParam("trainerId") int trainerId) {
        return dbTrainer.delete(trainerId);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Integer> create(DbCrudTrainer.EntityTrainer trainer) {
        log.debug("Add trainer {}", trainer);
        return dbTrainer.add(trainer);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Void> update(DbCrudTrainer.EntityTrainer trainer) {
        return dbTrainer.update(trainer);
    }
}
