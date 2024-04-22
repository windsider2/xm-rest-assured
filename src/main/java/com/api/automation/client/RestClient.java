package com.api.automation.client;


import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.springframework.stereotype.Service;

import static io.restassured.config.ConnectionConfig.connectionConfig;
import static io.restassured.config.HttpClientConfig.httpClientConfig;

@Service
public class RestClient {

    private RestClient() {
        RestAssured.config().httpClient(httpClientConfig().reuseHttpClientInstance());
        RestAssured.config().connectionConfig(connectionConfig().closeIdleConnectionsAfterEachResponse());
    }

    public RequestSpecification getClient() {
        return RestAssured.given();
    }
}
