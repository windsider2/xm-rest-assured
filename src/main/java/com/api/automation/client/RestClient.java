package com.api.automation.client;


import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.springframework.stereotype.Service;

import static com.api.automation.utils.ConfigSetter.*;
import static io.restassured.config.ConnectionConfig.*;
import static io.restassured.config.HttpClientConfig.*;

@Service
public class RestClient {

    protected RestClient() {
        RestAssured.config().httpClient(httpClientConfig().reuseHttpClientInstance());
        RestAssured.config().connectionConfig(connectionConfig().closeIdleConnectionsAfterEachResponse());
        RestAssured.baseURI = getConfigValue("base_url").toString();
    }

    public RequestSpecification buildRequest() {
        return RestAssured.given().log().all();
    }
}
