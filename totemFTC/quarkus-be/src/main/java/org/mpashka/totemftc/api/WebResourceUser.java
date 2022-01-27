package org.mpashka.totemftc.api;

import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

@Path("/api/user")
@Authenticated
public class WebResourceUser {

    private static final Logger log = LoggerFactory.getLogger(WebResourceUser.class);

    @Inject
    DbUser dbUser;

    @Inject
    WebSessionService webSessionService;

    @GET
    @Path("current")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<DbUser.EntityUser> currentUser() {
        int userId = webSessionService.getUserId();
        return dbUser.getUser(userId, true);
    }

    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({MySecurityProvider.ROLE_TRAINER, MySecurityProvider.ROLE_ADMIN})
    public Uni<DbUser.EntityUser[]> listUsers() {
        return dbUser.getAllUsers();
    }

    @GET
    @Path("listTrainers")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<DbUser.EntityUser[]> listTrainers() {
        return dbUser.getTrainers();
    }

    @DELETE
    @Path("delete/{userId}")
    @RolesAllowed(MySecurityProvider.ROLE_ADMIN)
    public Uni<Void> delete(@PathParam("userId") int userId) {
        return dbUser.deleteUser(userId);
    }

    @DELETE
    @Path("current/network/{networkId}")
    public Uni<Void> deleteNetwork(@PathParam("networkId") String networkId) {
        return dbUser.deleteUserSocialNetwork(webSessionService.getUserId(), networkId);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(MySecurityProvider.ROLE_ADMIN)
    public Uni<Integer> create(DbUser.EntityUser user) {
        log.debug("Add user {}", user);
        return dbUser.addUser(user)
                .onItem().transform(DbUser.EntityUser::getUserId);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(MySecurityProvider.ROLE_ADMIN)
    public Uni<Void> update(DbUser.EntityUser user) {
        return dbUser.updateUser(user);
    }

    @PUT
    @Path("current/nick")
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Void> updateCurrentUserNick(DbUser.EntityUser user) {
        return dbUser.updateUserNick(webSessionService.getUserId(), user.getNickName());
    }

    @PUT
    @Path("current/name")
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Void> updateCurrentUserName(DbUser.EntityUser user) {
        if (!webSessionService.getUser().getTypes().isEmpty()) {
            throw new SecurityException("Only guest is allowed to change name");
        }
        user.setUserId(webSessionService.getUserId());
        return dbUser.updateUserName(user);
    }

}
