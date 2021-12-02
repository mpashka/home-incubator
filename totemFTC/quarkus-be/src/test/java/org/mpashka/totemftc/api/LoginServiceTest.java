package org.mpashka.totemftc.api;

import io.quarkus.test.junit.QuarkusTest;
import io.vertx.mutiny.sqlclient.PreparedQuery;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlClient;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.LoggerFactory;

import java.util.logging.Logger;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class LoginServiceTest {


/*
    @Test
    public void testHelloEndpoint() {
        given()
          .when().get("/login")
          .then()
             .statusCode(200)
             .body(is("Hello RESTEasy"));
    }
*/

}
