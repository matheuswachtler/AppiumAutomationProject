package utils.api;

import static io.restassured.RestAssured.given;
import io.restassured.response.Response;
import java.util.Map;

public class LoginApi extends Specifications {

    public Map<String, String> requestLoginApi() {

        Response response = given()
                .spec(headerSpecificationLogin)
                .log().all()
        .when()
                .get(BASE_ENDPOINT_LOGIN)
        .then()
                .spec(responseSpecification)
                .log().all()
                .extract().response();

        Map<String, String> credentials = response.jsonPath().getMap("");
        return credentials;
    }
}