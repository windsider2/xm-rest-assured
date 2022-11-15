package com.api.automation.client;


import static com.api.automation.utils.ConfigSetter.getConfigValue;
import static io.restassured.config.ConnectionConfig.connectionConfig;
import static io.restassured.config.HttpClientConfig.httpClientConfig;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.springframework.stereotype.Service;

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
