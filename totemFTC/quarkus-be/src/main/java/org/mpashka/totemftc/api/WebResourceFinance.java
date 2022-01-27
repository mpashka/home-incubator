package org.mpashka.totemftc.api;

import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.time.LocalDate;

@Path("/api/finance")
@Authenticated
public class WebResourceFinance {

    private static final Logger log = LoggerFactory.getLogger(WebResourceFinance.class);

    @Inject
    WebSessionService webSessionService;

    @Inject
    DbCrudFinance dbFinance;

    @GET
    @Path("currentTrainerIncome/{period}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(MySecurityProvider.ROLE_TRAINER)
    public Uni<DbCrudFinance.EntityIncome> currentTrainerIncome(@RestPath DbCrudFinance.PeriodType period, @RestQuery LocalDate from) {
        return dbFinance.getIncomeForTrainer(period, from, webSessionService.getUserId());
    }

    @GET
    @Path("trainerIncome/{period}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(MySecurityProvider.ROLE_ADMIN)
    public Uni<DbCrudFinance.EntityIncome[]> trainerIncome(@RestPath DbCrudFinance.PeriodType period, @RestQuery LocalDate from) {
        return dbFinance.getTrainerIncome(period, from);
    }

    @GET
    @Path("totalIncome/{period}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(MySecurityProvider.ROLE_ADMIN)
    public Uni<DbCrudFinance.EntityIncome[]> totalIncome(@RestPath DbCrudFinance.PeriodType period, @RestQuery LocalDate from) {
        return dbFinance.getTotalIncome(period, from);
    }
}
