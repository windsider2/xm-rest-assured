package com.api.automation.client;


import io.restassured.response.Response;
import org.springframework.stereotype.Service;

import static io.restassured.RestAssured.*;

@Service
public class RestClient extends BaseConfig {

    public Response get(String path, Object... var2) {
        return when().get(path, var2);
    }

    public Response post(Object body, String path, Object... var2) {
        return given().body(body).post(path, var2);
    }

    public <T> T extract(Response response, Class<T> model) {
        return response.then().extract().as(model);
    }
}
