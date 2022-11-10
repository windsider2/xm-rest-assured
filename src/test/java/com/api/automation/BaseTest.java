package com.api.automation;

import com.api.automation.client.RestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

public class BaseTest extends AbstractTestNGSpringContextTests {
    @Autowired
    protected RestClient restClient;
}