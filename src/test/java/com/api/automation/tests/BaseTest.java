package com.api.automation.tests;

import com.api.automation.client.RestClient;
import io.restassured.specification.RequestSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

@SpringBootTest(classes = RestClient.class)
public abstract class BaseTest extends AbstractTestNGSpringContextTests {
    @Autowired
    private RestClient restClient;

    @Value("${base_url}")
    private String baseUrl;

    public RequestSpecification getRequestSpecification() {
        return restClient.getClient().baseUri(baseUrl).log().all();
    }
}