package com.rest_assured.client;

import static io.restassured.RestAssured.*;

public abstract class BaseConfig {
    protected BaseConfig(){
        baseURI = "https://jsonplaceholder.typicode.com";
    }
}

