package com.api.automation.client;

import org.springframework.stereotype.Component;

import static com.api.automation.utils.ConfigSetter.*;
import static io.restassured.RestAssured.*;
import static io.restassured.config.ConnectionConfig.*;
import static io.restassured.config.HttpClientConfig.*;

@Component
public class BaseConfig {

    protected BaseConfig() {
        config().httpClient(httpClientConfig().reuseHttpClientInstance());
        config().connectionConfig(connectionConfig().closeIdleConnectionsAfterEachResponse());
        baseURI = getConfigValue("base_url").toString();
    }
}

