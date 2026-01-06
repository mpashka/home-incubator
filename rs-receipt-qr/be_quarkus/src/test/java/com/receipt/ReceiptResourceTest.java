package com.receipt;



import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class ReceiptResourceTest {
    private static final Logger log = LoggerFactory.getLogger(ReceiptResourceTest.class);

    @Inject
    ReceiptResource receiptResource;

    @Test
    void testHelloEndpoint() {
        given()
                .contentType(ContentType.JSON)
//                .param("query", "Dune")
                .when().get("/api/receipts")
                .then().statusCode(200)
                .body(is("[]"));
    }

    @Test
    void testResource() {
        var receipts = receiptResource.getAll();
        log.info("Receipts: {}", receipts);
    }

}