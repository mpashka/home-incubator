package org.mpashka.totemftc.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

/**
 * todo move dbtest from quarkus-test1
 * todo use testcontainers
 */
//@QuarkusTest
//@QuarkusTestResource(PostgresResource.class)
public class DbTest {
    private static final Logger log = LoggerFactory.getLogger(DbTest.class);

    @Inject
    DbCrudVisit dbVisit;

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
}
