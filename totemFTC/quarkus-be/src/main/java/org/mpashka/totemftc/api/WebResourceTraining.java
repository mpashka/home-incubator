package org.mpashka.totemftc.api;

import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import org.jboss.resteasy.reactive.RestPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;

@Path("/api")
@Authenticated
public class WebResourceTraining {

    private static final Logger log = LoggerFactory.getLogger(WebResourceTraining.class);

    @Inject
    WebSessionService webSessionService;

    @Inject
    DbUser dbUser;

    @Inject
    DbCrudTraining dbTraining;

    @GET
    @Path("trainers/list")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<DbUser.EntityUser[]> listTrainers() {
        return dbUser.getTrainers();
    }

    @GET
    @Path("trainingType/list")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<DbCrudTraining.EntityTrainingType[]> listTrainingTypes() {
        return dbTraining.getTrainingTypes();
    }

    @POST
    @Path("trainingType")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Void> addTrainingTypes(DbCrudTraining.EntityTrainingType trainingType) {
        return dbTraining.addTrainingType(trainingType);
    }

    @PUT
    @Path("trainingType")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Void> updateTrainingTypes(DbCrudTraining.EntityTrainingType trainingType) {
        return dbTraining.updateTrainingType(trainingType);
    }

    @DELETE
    @Path("trainingType/{trainingType}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Void> deleteTrainingTypes(@RestPath String trainingType) {
        return dbTraining.deleteTrainingType(trainingType);
    }

    /**
     * todo [!] check this. How tf is this used.
     */
    @GET
    @Path("training/list")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<DbCrudTraining.Entity[]> list() {
        return dbTraining.getAll();
    }

    @GET
    @Path("training/byDate/{date}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<DbCrudTraining.Entity[]> byDate(@RestPath LocalDate date) {
        return dbTraining.getByDate(date);
    }

    @GET
    @Path("userTraining/byDateInterval")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<DbCrudTraining.Entity[]> userTrainingsByDateInterval(@QueryParam("from") LocalDateTime from, @QueryParam("to") LocalDateTime to) {
        return dbTraining.getByDateIntervalForUser(from, to);
    }

    @GET
    @Path("masterTraining/byDateInterval")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(MySecurityProvider.ROLE_TRAINER)
    public Uni<DbCrudTraining.Entity[]> masterTrainingsByDateInterval(@QueryParam("from") LocalDateTime from, @QueryParam("to") LocalDateTime to) {
        return dbTraining.getByDateIntervalForTrainer(webSessionService.getUserId(), from, to);
    }

    @DELETE
    @Path("training/{trainingId}")
    @RolesAllowed({MySecurityProvider.ROLE_TRAINER, MySecurityProvider.ROLE_ADMIN})
    public Uni<Void> delete(@PathParam("trainingId") int trainerId) {
        EnumSet<DbUser.UserType> userTypes = webSessionService.getUser().getTypes();
        return userTypes.contains(DbUser.UserType.admin) ? dbTraining.delete(trainerId) : dbTraining.delete(trainerId, webSessionService.getUserId());
    }

    @POST
    @Path("training")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({MySecurityProvider.ROLE_TRAINER, MySecurityProvider.ROLE_ADMIN})
    public Uni<Integer> create(DbCrudTraining.Entity training) {
        log.debug("Add training {}", training);
        checkTrainerAccess(training);
        return dbTraining.add(training);
    }

    @PUT
    @Path("training")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({MySecurityProvider.ROLE_TRAINER, MySecurityProvider.ROLE_ADMIN})
    public Uni<Void> update(DbCrudTraining.Entity training) {
        checkTrainerAccess(training);
        return dbTraining.update(training, isAdmin());
    }

    @PUT
    @Path("training/comment")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({MySecurityProvider.ROLE_TRAINER, MySecurityProvider.ROLE_ADMIN})
    public Uni<Void> updateComment(DbCrudTraining.Entity training) {
        checkTrainerAccess(training);
        return dbTraining.updateComment(training, isAdmin());
    }

    private void checkTrainerAccess(DbCrudTraining.Entity training) {
        EnumSet<DbUser.UserType> userTypes = webSessionService.getUser().getTypes();
        if (userTypes.contains(DbUser.UserType.admin)) {
            return;
        } else if (!userTypes.contains(DbUser.UserType.trainer)) {
            throw new NotAuthorizedException("Trying to access privileged method without proper permissions");
        }
        if (training.getTrainer().getUserId() != webSessionService.getUserId()) {
            throw new NotAuthorizedException("Trying to access inappropriate resource");
        }
    }

    private boolean isAdmin() {
        EnumSet<DbUser.UserType> userTypes = webSessionService.getUser().getTypes();
        return userTypes.contains(DbUser.UserType.admin);
    }
}
