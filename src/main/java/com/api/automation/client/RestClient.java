package com.api.automation.client;


import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.springframework.stereotype.Service;

@Service
public class RestClient extends BaseConfig {

    public RequestSpecification buildRequest() {
        return RestAssured.given();
    }
}
