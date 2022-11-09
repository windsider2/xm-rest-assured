package com.rest_assured.client;


import io.restassured.response.ValidatableResponse;
import org.springframework.stereotype.Service;

import static io.restassured.RestAssured.*;

@Service
public class RestClient extends BaseConfig {
    public ValidatableResponse get(String path, int status) {
        return when().get(path).then().statusCode(status);
    }

    public <T> T extract(ValidatableResponse response, Class<T> pojo) {
        return response.extract().as(pojo);
    }
}
