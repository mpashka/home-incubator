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

@Path("/api/user")
@Authenticated
public class WebResourceUser {

    private static final Logger log = LoggerFactory.getLogger(WebResourceUser.class);

    @Inject
    DbUser dbUser;

    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<DbUser.EntityUser[]> listTrainers() {
        return dbUser.getAllUsers();
    }

    @DELETE
    @Path("{userId}")
    public Uni<Void> delete(@PathParam("userId") int userId) {
        return dbUser.deleteUser(userId);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Integer> create(DbUser.EntityUser user) {
        log.debug("Add user {}", user);
        return dbUser.addUser(user);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Void> update(DbUser.EntityUser user) {
        return dbUser.updateUser(user);
    }

}
