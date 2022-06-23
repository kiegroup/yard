package org.kie.yard.impl1.jitexecutor;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
public class SmokeTest {
    @Test
    public void testjitEndpoint() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"Age\": 47, \"Previous incidents?\": false}")
                .when().post("/yard")
                .then()
                .statusCode(200)
                .body("'Base price'", is(500));
    }
}
