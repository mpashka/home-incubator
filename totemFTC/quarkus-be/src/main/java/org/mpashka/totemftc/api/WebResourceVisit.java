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
 * todo [!] This must be removed
 */
@Path("/api/visit")
@Authenticated
public class WebResourceVisit {

    private static final Logger log = LoggerFactory.getLogger(WebResourceVisit.class);

    @Inject
    DbCrudVisit dbVisit;

/*
    @GET
    @Path("{visitId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<DbCrudTrainer.Entity> get(@PathParam("visitId") int trainerId) {
        return dbTrainer.getById(trainerId);
    }
*/

    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<DbCrudVisit.EntityVisit[]> list() {
        return dbVisit.getAll();
    }

    @DELETE
    @Path("{visitId}")
    public Uni<Void> delete(@PathParam("visitId") int visitId) {
        return dbVisit.delete(visitId);
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
        return dbVisit.update(visit);
    }
}
