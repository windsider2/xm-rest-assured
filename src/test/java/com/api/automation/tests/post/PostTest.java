package com.api.automation.tests.post;

import com.api.automation.dataprovider.PostDataProvider;
import com.api.automation.model.Post;
import com.api.automation.tests.BaseTest;
import io.restassured.http.Headers;
import io.restassured.response.ValidatableResponse;
import org.springframework.beans.factory.annotation.Value;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.stream.IntStream;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;

public class PostTest extends BaseTest {

    @Value("${post_id_formatter}")
    private String postIdFormatter;

    /**
     * This test verifies the SSL certificate validation behavior when making a GET request to the specified base URL.
     * It relaxes the HTTPS validation to accommodate testing against environments with self-signed certificates
     * or other scenarios where strict certificate validation is not required.
     */
    @Test
    public void verifyCertificateTest() {
        getRequestSpecification()
                .given()
                .relaxedHTTPSValidation()
                .when()
                .get()
                .then()
                .assertThat()
                .statusCode(SC_OK);
    }

    /**
     This test verifies the status code and schema of all returned posts.
     It sends a GET request to retrieve posts and then validates the response against a predefined JSON schema.
     */
    @Test
    public void verifyPostsSchemaTest() {
        getRequestSpecification().get()
                .then()
                .assertThat()
                .statusCode(SC_OK)
                .body(matchesJsonSchemaInClasspath("posts-schema.json"));
    }

    /**
     This test verifies the status code and response time of the GET request to retrieve posts.
     It sends a GET request to retrieve posts and then asserts that the response status code is OK (200)
     and that the response time is less than 1000 milliseconds.
     */
    @Test
    public void verifyGetPostsResponseTimeTest() {
        getRequestSpecification().get()
                .then()
                .assertThat()
                .statusCode(SC_OK)
                .time(lessThan(1000L));
    }

    /**
     This test verifies the status code and data of a returned post object by executing a GET request.
     It uses a data provider to set data for both positive (with existing ID) and negative (with non-existing ID) cases.
     The test executes a single GET request and verifies that the response status code and data match the expected values.
     @param postId The ID of the post to retrieve.
     @param statusCode The expected status code of the response.
     @param expectedPost The expected Post object representing the retrieved post.
     */

    @Test(dataProvider = "postProvider", dataProviderClass = PostDataProvider.class)
    public void getPostObjectDataTest(int postId, int statusCode, Post expectedPost) {
        executeNumberOfGetRequestsAndVerifyResponseData(1, postId, statusCode, expectedPost);
    }

    /**
     This test verifies idempotence by sending multiple identical requests to the server and checking for any state changes.
     It uses a data provider to set data for the requests, including the post ID, expected status code, and expected post data.
     The test sends a specified number of identical GET requests to the server and verifies that the responses match the expected data.
     @param postId The ID of the post for which idempotence is being verified.
     @param statusCode The expected status code of the response.
     @param expectedPost The expected Post object representing the retrieved post.
     */

    @Test(dataProvider = "postProviderForIdempotence", dataProviderClass = PostDataProvider.class)
    public void verifyIdempotenceTest(int postId, int statusCode, Post expectedPost) {
        final int numberOfRequests = 2;
        executeNumberOfGetRequestsAndVerifyResponseData(numberOfRequests, postId, statusCode, expectedPost);
    }

    /**
     * Test method to verify status code and returned body after executing a post request.
     *
     * <p>The test verifies the status code and returned body after sending a post request.
     * The test fails after get request to retrieve the created resource as the application under test does not allow to create resources.
     * In the negative case, an invalid payload is sent, resulting in the server returning a 201 code, which
     * should actually be 400 (bad request).
     *
     * @param postId                 The ID of the post object.
     * @param bodyToPost             The payload to be sent in the post request.
     * @param statusCode             The expected status code of the response.
     * @param expectedResponseBody  The expected response body.
     */
    @Test(dataProvider = "postBodyProvider", dataProviderClass = PostDataProvider.class)
    public void postPostObjectTest(int postId, String bodyToPost, int statusCode, String expectedResponseBody) {
        final String response = getRequestSpecification()
                .body(bodyToPost)
                .post()
                .then()
                .assertThat()
                .statusCode(statusCode)
                .extract().asPrettyString();
        Assert.assertEquals(response.replaceAll("\\s", ""), expectedResponseBody);

        if (statusCode == SC_CREATED) {
            final String actualResponseBody = getRequestSpecification()
                    .get(postIdFormatter, postId)
                    .then()
                    .assertThat()
                    .statusCode(SC_OK)
                    .extract()
                    .asPrettyString();
            Assert.assertEquals(actualResponseBody.replaceAll("\\s", ""), bodyToPost, "Returned body after post request does not match the expected");
        }
    }

    /**
     * Test method to verify status code and returned body after executing a PUT request.
     *
     * <p>The test verifies the status code and returned body after sending a PUT request. In the
     * negative case, a non-existing post ID is used. The test fails after a get request of the
     * updated resource as the application under test does not allow updating objects.
     *
     * @param postId                 The ID of the post object.
     * @param bodyToPost             The payload to be sent in the PUT request.
     * @param statusCode             The expected status code of the response.
     * @param expectedResponseBody  The expected response body.
     */

    @Test(dataProvider = "putBodyProvider", dataProviderClass = PostDataProvider.class)
    public void putPostObjectTest(int postId, String bodyToPost, int statusCode, String expectedResponseBody) {

        final String response = getRequestSpecification()
                .body(bodyToPost)
                .put(postIdFormatter, postId)
                .then()
                .assertThat()
                .statusCode(statusCode)
                .extract().asString();
        Assert.assertTrue(response.contains(expectedResponseBody));
        //for positive test verification of updated resource with get request
        if (statusCode == SC_OK) {
            final String getResponseBody = getRequestSpecification()
                    .get(postIdFormatter, postId)
                    .then()
                    .assertThat()
                    .statusCode(SC_OK)
                    .extract()
                    .asString();
            Assert.assertEquals(getResponseBody.replaceAll("\\s", ""), bodyToPost, "Returned body after put request does not match the expected");
        }
    }

    /**
     * Test method to verify status code and returned body after executing a patch request.
     *
     * <p>The test verifies the status code and returned body after sending a patch request.
     * The test to fail as the application under test does not allow updating resources.
     */
    @Test
    public void patchPostObjectTest() {

        final int postId = 1;
        final ValidatableResponse response = getRequestSpecification()
                .body("{\"title\": \"updated title\"}")
                .patch(postIdFormatter, postId)
                .then()
                .assertThat()
                .statusCode(SC_OK)
                .body("title", equalTo("updated title"));
    }

    /**
     * Test method to verify status code and returned body after executing a delete request.
     *
     * <p>The test verifies the status code and returned body after sending a delete request.
     * The test fails as the application under test does not allow deleting resources.
     */
    @Test
    public void deletePostObjectTest() {

        final int postId = 1;
        final String response = getRequestSpecification()
                .delete(postIdFormatter, postId)
                .then()
                .assertThat()
                .statusCode(SC_OK)
                .extract().asString();

        Assert.assertEquals(response, "{}", "Response body after delete request does not match the expected");
    }

    /**
     * Test method to verify the response time of the last post request.
     *
     * <p>The test verifies the status code and response time of the last post. It first retrieves
     * the total number of posts and then sends a get request to the last post's endpoint to verify
     * the response time.
     */
    @Test
    public void verifyGetResponseTimeTest() {

        final long size = getRequestSpecification()
                .get()
                .then()
                .assertThat()
                .statusCode(SC_OK)
                .extract().as(Post[].class)
                .length;

        getRequestSpecification()
                .given()
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .get("/" + size)
                .then()
                .assertThat()
                .statusCode(SC_OK)
                .time(lessThan(1000L));
    }

    /**
     * Test method to verify the headers of a response received after making a GET request.
     */
    @Test
    public void verifyHeadersTest() {
        Headers headers = getRequestSpecification()
                .header("Server-Timing", "cf-q-config")
                .get()
                .then()
                .assertThat()
                .statusCode(SC_OK)
                .extract().headers();

        final LocalDateTime actualDate = LocalDateTime.parse(headers.get("Date").getValue(), RFC_1123_DATE_TIME).truncatedTo(MINUTES);
        final LocalDateTime expectedDate = ZonedDateTime.now(ZoneId.of("GMT")).truncatedTo(MINUTES).toLocalDateTime();
        final String actualContentType = headers.get("Content-Type").getValue();
        final String actualConnection = headers.get("Connection").getValue();
        final String actualExpires = headers.get("Expires").getValue();
        final String actualCacheControl = headers.get("Cache-Control").getValue();

        SoftAssert soft = new SoftAssert();
        soft.assertEquals(actualDate, expectedDate, "Date header value does not match the expected");
        soft.assertEquals(actualContentType, "application/json; charset=utf-8", "Content Header type value does not match the expected");
        soft.assertEquals(actualConnection, "keep-alive", "Connection Header value does not match the expected");
        soft.assertEquals(actualExpires, "-1", "Expires Header value does not match the expected");
        soft.assertEquals(actualCacheControl, "max-age=43200", "Cache-Control Header value does not match the expected");
        soft.assertAll();
    }

    /**
     * Helper method to execute a specified number of GET requests and verify the response data.
     *
     * @param numberOfExecutions  The number of GET requests to execute.
     * @param postId              The ID of the post object.
     * @param statusCode          The expected status code of the response.
     * @param expectedPost        The expected post object.
     */
    private void executeNumberOfGetRequestsAndVerifyResponseData(int numberOfExecutions, int postId, int statusCode, Post expectedPost) {
        IntStream.range(0, numberOfExecutions).forEach(request ->
                {
                    final Post actualPost = getRequestSpecification()
                            .get(postIdFormatter, postId)
                            .then()
                            .assertThat()
                            .statusCode(statusCode)
                            .extract().as(Post.class);
                    Assert.assertEquals(actualPost, expectedPost, "Post with id " + postId + " does not match the expected");
                }
        );
    }
}
