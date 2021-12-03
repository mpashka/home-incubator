package org.mpashka.totemftc.api;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.HttpHeaders;

import static io.restassured.RestAssured.given;

//@QuarkusTest
public class FunctionalTest {
//    @Test
    public void loginUserInfo() {
        given()
                .when()
                .baseUri("/api/user")
                .header(HttpHeaders.AUTHORIZATION, "Bearer session_10001")
                .body("""
              {
                "title" : "Learn Quarkus",
                "priority" : 1,
              }
              """)
                .then()
                .statusCode(200)
                .body(
                        Matchers.is("aaa")
/*
                        matchesJson(
                                """
                                {
                                  "id" : 1,
                                  "title" : "Learn Quarkus",
                                  "priority" : 1,
                                  "completed" : false,
                                }
                                """)
*/
                )
        ;
    }

    //    @Test
    public void createTodoShouldYieldId() {
        given()
                .when()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer 10")
                .body("""
              {
                "title" : "Learn Quarkus",
                "priority" : 1,
              }
              """)
                .then()
                .statusCode(201)
                /*
                .body(
                        matchesJson(
                                """
                                {
                                  "id" : 1,
                                  "title" : "Learn Quarkus",
                                  "priority" : 1,
                                  "completed" : false,
                                }
                                """))
                */
        ;
    }
}
