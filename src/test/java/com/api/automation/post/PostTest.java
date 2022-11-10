package com.api.automation.post;

import com.api.automation.BaseTest;
import com.api.automation.client.RestClient;
import com.api.automation.dataprovider.PostDataProvider;
import com.api.automation.model.Post;
import io.restassured.response.Response;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.stream.IntStream;

import static com.api.automation.utils.ConfigSetter.*;
import static io.restassured.module.jsv.JsonSchemaValidator.*;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(classes = RestClient.class)
public class PostTest extends BaseTest {
    private static final String PATH_TO_POSTS = getConfigValue("post_path").toString();

    @Test
    public void verifyPostsSchemaTest() {
        //the test verifies status code and schema of all returned posts
        restClient.get(PATH_TO_POSTS)
                .then()
                .assertThat()
                .statusCode(SC_OK)
                .body(matchesJsonSchemaInClasspath("posts-schema.json"));
    }

    @Test
    public void verifyPostsResponseTimeTest() {
        //the test verifies status code and response time
        restClient.get(PATH_TO_POSTS)
                .then()
                .assertThat()
                .statusCode(SC_OK)
                .time(lessThan(200L));
    }

    @Test(dataProvider = "postProvider", dataProviderClass = PostDataProvider.class)
    public void verifyPostDataTest(int postId, int statusCode, Post expectedPost) {
        //the test verifies status code and data of a returned post
        //to set data for positive (with existing id) and negative (with not existing id) cases data provider is used
        executeGetRequestAndVerifyResponseData(1, postId, statusCode, expectedPost);
    }

    @Test(dataProvider = "postProviderForIdempotence", dataProviderClass = PostDataProvider.class)
    public void verifyGetMethodIdempotenceTest(int statusCode, Post expectedPost) {
        //the test verifies there is no state change in the system (idempotence) after some the same requests
        final int numberOfRequests = 2;
        final int postId = 1;
        executeGetRequestAndVerifyResponseData(numberOfRequests, postId, statusCode, expectedPost);
    }

    @Test(dataProvider = "postBodyProvider", dataProviderClass = PostDataProvider.class)
    public void verifyPostCreationTest(String path, Object bodyToPost, int statusCode, String expectedResponseBody) {
        final Response response = restClient.post(bodyToPost, PATH_TO_POSTS + path);
        response.then()
                .assertThat()
                .statusCode(statusCode);
        Assert.assertEquals(response.asPrettyString().replaceAll("\\s", ""), expectedResponseBody);
    }

    @Test
    public void verifyLastPostResponseTimeTest() {
        //the test verifies status code and response time of last post
        final Response response = restClient
                .get(PATH_TO_POSTS);
        final long size = restClient.extract(response, Post[].class).length;
        restClient
                .get(PATH_TO_POSTS + "/" + size)
                .then()
                .assertThat()
                .statusCode(SC_OK)
                .time(lessThan(200L));
    }

    private void executeGetRequestAndVerifyResponseData(int numberOfExecutions, int id, int statusCode, Post expectedPost) {
        IntStream.range(0, numberOfExecutions).forEach(time ->
                {
                    final Response response = restClient.get(PATH_TO_POSTS + "/{id}", id);
                    response.then()
                            .assertThat()
                            .statusCode(statusCode);
                    final Post actualPost = restClient.extract(response, Post.class);
                    Assert.assertEquals(actualPost, expectedPost, "Post with id " + id + " does not match the expected");
                }
        );

    }
}
