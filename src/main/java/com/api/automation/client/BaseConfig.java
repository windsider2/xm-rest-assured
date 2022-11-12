package com.api.automation.client;

import io.restassured.RestAssured;

import static com.api.automation.utils.ConfigSetter.*;
import static io.restassured.config.ConnectionConfig.*;
import static io.restassured.config.HttpClientConfig.*;

public abstract class BaseConfig {

    protected BaseConfig() {
        RestAssured.config().httpClient(httpClientConfig().reuseHttpClientInstance());
        RestAssured.config().connectionConfig(connectionConfig().closeIdleConnectionsAfterEachResponse());
        RestAssured.baseURI = getConfigValue("base_url").toString();
    }
}

