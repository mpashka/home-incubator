package org.mpashka.totemftc.api;

import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import org.jboss.resteasy.reactive.RestPath;
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
import java.time.LocalDate;

@Path("/api")
@Authenticated
public class WebResourceTicket {

    private static final Logger log = LoggerFactory.getLogger(WebResourceTicket.class);

    @Inject
    DbCrudTicket dbTicket;

    @Inject
    WebSessionService.RequestParameters requestParameters;

    @GET
    @Path("ticketTypes/list")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<DbCrudTicket.EntityTicketType[]> listTicketTypes() {
        return dbTicket.getTicketTypes();
    }

    @GET
    @Path("tickets/byUser")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<DbCrudTicket.EntityTicket[]> listTicketsByUser() {
        return dbTicket.getTicketsByUser(requestParameters.getSession().getUserId());
    }

    @GET
    @Path("tickets/byUser/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<DbCrudTicket.EntityTicket[]> listTicketsByUser(@PathParam("userId") int userId) {
        return dbTicket.getTicketsByUser(userId);
    }

    @GET
    @Path("ticket/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<DbCrudTicket.EntityTicket> getTicketById(@RestPath int id) {
        return dbTicket.getTicketById(id);
    }

    @DELETE
    @Path("ticket/{id}")
    public Uni<Void> delete(@RestPath int id) {
        return dbTicket.deleteTicket(id);
    }

    @POST
    @Path("ticket")
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Integer> create(DbCrudTicket.EntityTicket ticket) {
        log.debug("Add ticket {}", ticket);
        return dbTicket.addTicket(ticket);
    }
}
