package utils.api;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import utils.ConfigReader;

public class Specifications {

    public static RequestSpecification headerSpecificationLogin;
    public static ResponseSpecification responseSpecification;

    public static final String BASE_ENDPOINT_LOGIN = ConfigReader.getProperty("api.base.url");

    static {
        headerSpecificationLogin = new RequestSpecBuilder()
                .setBaseUri(BASE_ENDPOINT_LOGIN)
                .setContentType(ContentType.JSON)
                .build();

        responseSpecification = new ResponseSpecBuilder()
                .expectStatusCode(200)
                .expectContentType(ContentType.JSON)
                .build();
    }
}