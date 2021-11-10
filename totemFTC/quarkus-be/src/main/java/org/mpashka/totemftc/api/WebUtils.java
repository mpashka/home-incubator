package org.mpashka.totemftc.api;

import io.smallrye.mutiny.Uni;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.time.LocalDateTime;

@Path("/api/utils")
public class WebUtils {

    @Inject
    DbSchedulePropagate dbSchedulePropagate;

    @GET
    @Path("schedulePropagate")
    public Uni<Void> schedulePropagate() {
        return dbSchedulePropagate.schedulePropagate();
    }
}
