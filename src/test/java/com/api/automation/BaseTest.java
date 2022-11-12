package com.api.automation;

import com.api.automation.client.RestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

@SpringBootTest(classes = RestClient.class)
public abstract class BaseTest extends AbstractTestNGSpringContextTests {
    @Autowired
    protected RestClient restClient;
}