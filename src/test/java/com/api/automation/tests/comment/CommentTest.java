package com.api.automation.tests.comment;

import com.api.automation.dataprovider.CommentDataProvider;
import com.api.automation.model.Comment;
import com.api.automation.tests.BaseTest;
import io.restassured.http.Headers;
import org.springframework.beans.factory.annotation.Value;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.lessThan;

public class CommentTest extends BaseTest {

    @Value("${post_path}")
    private String pathToPosts;

    @Value("${post_id_formatter}")
    private String postIdFormatter;

    @Value("${comments_path}")
    private String pathToComments;

    /**
     * Test method to verify schema of all returned comments.
     *
     * @implNote The post ID is hardcoded as 1 for demonstration purposes.
     */
    @Test
    public void verifyCommentsSchemaTest() {
        final long postId = 1;
        getRequestSpecification()
                .get(postIdFormatter + pathToComments, postId)
                .then()
                .assertThat()
                .statusCode(SC_OK)
                .body(matchesJsonSchemaInClasspath("comments-schema.json"));
    }

    /**
     * Test method to verify response time of GET request for posts.
     * <p>
     * The response time is expected to be less than 1000 milliseconds.
     */
    @Test
    public void verifyGetPostsResponseTimeTest() {

        final long postId = 1;
        getRequestSpecification()
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .get(postIdFormatter + pathToComments, postId)
                .then()
                .assertThat()
                .statusCode(SC_OK)
                .time(lessThan(1000L));
    }

    /**
     * Test method to verify post ID in comments is equal to the requested post ID.
     */
    @Test
    public void verifyPostIdInCommentsTest() {

        final long expectedPostId = 1;
        final List<Comment> comments = Arrays.asList(getRequestSpecification()
                .get(postIdFormatter + pathToComments, expectedPostId)
                .then()
                .statusCode(SC_OK)
                .extract().as(Comment[].class));

        Assert.assertFalse(comments.isEmpty(), "Comments list is empty");

        assertThat(comments).
                as("Post Id is not correct")
                .allMatch(comment ->
                comment.getPostId().equals(expectedPostId));

    }

    /**
     * Test method to verify responses from different paths.
     *
     * <p>The test verifies that responses from equivalent paths (/posts/{id}/comments and
     * /comments?postId={id}) are equal.
     */
    @Test
    public void verifyResponsesFromDifferentPathsTest() {

        final long expectedPostId = 1;
        final Comment[] comments = getRequestSpecification()
                .get(postIdFormatter + pathToComments, expectedPostId)
                .then()
                .statusCode(SC_OK)
                .extract().as(Comment[].class);

        final Comment[] commentsAfterParameterizedRequest = getRequestSpecification()
                .basePath("")
                .param("postId", expectedPostId)
                .get(pathToComments)
                .then()
                .statusCode(SC_OK)
                .extract().as(Comment[].class);

        Assert.assertEquals(commentsAfterParameterizedRequest, comments, "Comments list from equivalent paths responses are not the same");
    }

    /**
     * Test method to verify that each comment ID matches the serial number.
     *
     * <p>The test verifies that each comment ID matches its serial number. It retrieves comments for a
     * specific post ID and then compares each comment's ID with its position in the array incremented
     * by one.
     */
    @Test
    public void verifyCommentsIdsTest() {

        final Comment[] comments = getRequestSpecification()
                .basePath("")
                .param("postId", 1)
                .get(pathToComments)
                .then()
                .statusCode(SC_OK)
                .extract().as(Comment[].class);

        for (int i = 0; i < comments.length; i++) {
            Assert.assertEquals(comments[i].getId(), ++i);
        }
    }

    /**
     * Test method to verify idempotence in the server's responses.
     *
     * <p>The test verifies that there is no state change in the server (idempotence) after
     * sending equivalent requests, and the server returns the expected object.
     *
     * @param postId          The ID of the post associated with the comment.
     * @param commentId       The ID of the comment.
     * @param statusCode      The expected status code of the response.
     * @param expectedComment The expected comment object.
     */
    @Test(dataProvider = "commentProvider", dataProviderClass = CommentDataProvider.class)
    public void verifyIdempotenceTest(long postId, long commentId, int statusCode, Comment expectedComment) {

        final int numberOfRequests = 2;
        executeNumberOfGetRequestsAndVerifyResponseData(numberOfRequests, postId, commentId, statusCode, expectedComment);
    }

    /**
     * Test method to verify response headers.
     *
     * <p>The test verifies various response headers such as Date, Content-Type, Connection, Expires,
     * and Cache-Control.
     */
    @Test
    public void verifyHeadersTest() {
        Headers headers = getRequestSpecification()
                .basePath("")
                .header("Connection", "keep-alive")
                .header("Content-Type", "application/json; charset=utf-8")
                .param("postId", 1)
                .get(pathToComments)
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
     * Helper method to execute a specified number of get requests and verify the response data.
     *
     * <p>This method executes the specified number of get requests, each retrieving comment data based
     * on the provided post and comment IDs. The response data is then verified against the expected
     * comment object.
     *
     * @param numberOfRequests The number of get requests to execute.
     * @param postId           The ID of the post associated with the comments.
     * @param commentId        The ID of the comment to retrieve.
     * @param statusCode       The expected status code of the response.
     * @param expectedComment  The expected comment object to verify against the response.
     */
    private void executeNumberOfGetRequestsAndVerifyResponseData(int numberOfRequests, long postId, long commentId, int statusCode, Comment expectedComment) {
        IntStream.range(0, numberOfRequests).forEach(request ->
                {
                    final Comment actualComment = getRequestSpecification()
                            .param("id", commentId)
                            .get(postIdFormatter + pathToComments, postId)
                            .then()
                            .statusCode(statusCode)
                            .extract().jsonPath()
                            .getObject("[0]", Comment.class);

                    Assert.assertEquals(actualComment, expectedComment);
                }
        );
    }
}
