package org.mpashka.totemftc.api;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

/**
 * todo move dbtest from quarkus-test1
 * todo use testcontainers
 */
@QuarkusTest
@QuarkusTestResource(PostgresResource.class)
public class DbTest {
    private static final Logger log = LoggerFactory.getLogger(DbTest.class);

    @Inject
    DbCrudVisit dbVisit;

    @Inject
    DbCrudTicket dbTicket;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    WebSessionService webSessionService;

    @Test
    @Disabled
    void testMark() throws JsonProcessingException {
        DbCrudVisit.EntityVisit entityVisit = objectMapper.readValue("""
{
    "trainingId": 21,
    "user": {
        "userId": 10000
    },
    "markSchedule": false,
    "markSelf": "unmark",
    "markMaster": "unmark",
    "comment": "No comments"
}
                """, DbCrudVisit.EntityVisit.class);
        DbCrudTicket.EntityTicket ticket = dbVisit.updateMark(entityVisit, true, DbCrudVisit.EntityVisitMark.unmark, DbCrudVisit.EntityVisitMark.unmark)
                .onFailure().invoke(e -> log.error("Error mark", e))
                .await().atMost(Duration.of(1, ChronoUnit.MINUTES));
        int initial = ticket.getVisited();

        ticket = dbVisit.updateMark(entityVisit, null, DbCrudVisit.EntityVisitMark.on, null)
                .await().atMost(Duration.of(1, ChronoUnit.MINUTES));
        assertThat(ticket.getStart(), notNullValue());
        assertThat(ticket.getVisited(), is(initial + 1));

        ticket = dbVisit.updateMark(entityVisit, null, DbCrudVisit.EntityVisitMark.off, null)
                .await().atMost(Duration.of(1, ChronoUnit.MINUTES));
        if (initial == 0) {
            assertThat(ticket.getStart(), nullValue());
        }
        assertThat(ticket.getVisited(), is(initial));

        ticket = dbVisit.updateMark(entityVisit, null, DbCrudVisit.EntityVisitMark.unmark, null)
                .await().atMost(Duration.of(1, ChronoUnit.MINUTES));
        assertThat(ticket.getVisited(), is(initial));

        ticket = dbVisit.updateMark(entityVisit, null, null, DbCrudVisit.EntityVisitMark.on)
                .await().atMost(Duration.of(1, ChronoUnit.MINUTES));
        assertThat(ticket.getStart(), notNullValue());
        assertThat(ticket.getVisited(), is(initial + 1));

        ticket = dbVisit.updateMark(entityVisit, null, null, DbCrudVisit.EntityVisitMark.off)
                .await().atMost(Duration.of(1, ChronoUnit.MINUTES));
        assertThat(ticket.getVisited(), is(initial));

        ticket = dbVisit.updateMark(entityVisit, null, null, DbCrudVisit.EntityVisitMark.unmark)
                .await().atMost(Duration.of(1, ChronoUnit.MINUTES));
        assertThat(ticket.getVisited(), is(initial));
    }

    @Test
    @Disabled
    void testFetchSession() {
        WebSessionService.Session session = webSessionService.fetchSession("WJ7TEGW@.nT\"f6K_Mll^").await().indefinitely();
        if (session != null) {
            log.debug("User: {}", session.getUser());
        } else {
            log.debug("Session not found");
        }
    }

    @Test
    void testVisit() throws JsonProcessingException {
        ObjectMapper m = new ObjectMapper();
        m = m
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

/*
        m = m
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setVisibility(m.getSerializationConfig().getDefaultVisibilityChecker()
                                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
*/

        DbCrudVisit.EntityVisit[] visits;
        visits = dbVisit.getByTicket(-1, 3).await().indefinitely();
        log.debug("Visits: {}", m.writeValueAsString(visits));

        visits = dbVisit.getByTicket(10000, 3).await().indefinitely();
        log.debug("Visits: {}", m.writeValueAsString(visits));

        visits = dbVisit.getByTicket(10001, 3).await().indefinitely();
        log.debug("Visits: {}", m.writeValueAsString(visits));

        visits = dbVisit.getByTraining(80).await().indefinitely();
        log.debug("Visits: {}", m.writeValueAsString(visits));

        visits = dbVisit.getByUser(10000, LocalDateTime.now().minus(10, ChronoUnit.DAYS)).await().indefinitely();
        log.debug("Visits: {}", m.writeValueAsString(visits));
    }

    @Test
    void testTicket() {
        DbCrudTicket.EntityTicket[] ticketsByUser = dbTicket.getTicketsByUser(10000).await().indefinitely();
        log.debug("Tickets: {}", ticketsByUser);
    }
}
