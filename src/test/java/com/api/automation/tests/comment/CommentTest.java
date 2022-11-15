package com.api.automation.tests.comment;

import static com.api.automation.utils.ConfigSetter.getConfigValue;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.lessThan;

import com.api.automation.dataprovider.CommentDataProvider;
import com.api.automation.model.Comment;
import com.api.automation.tests.BaseTest;
import io.restassured.http.Headers;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

public class CommentTest extends BaseTest {
    private static final String PATH_TO_POSTS = getConfigValue("post_path").toString();
    private static final String POST_ID_FORMATTER = "/{postId}";
    private static final String PATH_TO_COMMENTS = getConfigValue("comments_path").toString();


    @Test
    public void verifyCommentsSchemaTest() {
        //the test verifies status code and schema of all returned comments schema
        final long postId = 1;
        restClient.buildRequest().get(PATH_TO_POSTS + POST_ID_FORMATTER + PATH_TO_COMMENTS, postId)
                .then()
                .assertThat()
                .statusCode(SC_OK)
                .body(matchesJsonSchemaInClasspath("comments-schema.json"));
    }

    @Test
    public void verifyGetPostsResponseTimeTest() {
        //the test verifies status code and response time
        final long postId = 1;
        restClient.buildRequest()
                .get(PATH_TO_POSTS + POST_ID_FORMATTER + PATH_TO_COMMENTS, postId)
                .then()
                .assertThat()
                .statusCode(SC_OK)
                .time(lessThan(1000L));
    }

    @Test
    public void verifyPostIdInCommentsTest() {
        //the test verifies post id in comments is equal to the requested post id
        final long expectedPostId = 1;
        final List<Comment> comments = Arrays.asList(restClient.buildRequest()
                .get(PATH_TO_POSTS + POST_ID_FORMATTER + PATH_TO_COMMENTS, expectedPostId)
                .then()
                .statusCode(SC_OK)
                .extract().as(Comment[].class));

        Assert.assertFalse(comments.isEmpty(), "Comments list is empty");
        comments.forEach(comment -> Assert.assertEquals(comment.getPostId(), expectedPostId));
    }

    @Test
    public void verifyResponsesFromDifferentPathsTest() {
        //the test verifies responses from equivalent paths (/posts/{id}/comments and /comments?postId={id}) are equal
        final long expectedPostId = 1;
        final Comment[] comments = restClient.buildRequest()
                .get(PATH_TO_POSTS + POST_ID_FORMATTER + PATH_TO_COMMENTS, expectedPostId)
                .then()
                .statusCode(SC_OK)
                .extract().as(Comment[].class);

        final Comment[] commentsAfterParameterizedRequest = restClient.buildRequest()
                .param("postId", expectedPostId)
                .get(PATH_TO_COMMENTS)
                .then()
                .statusCode(SC_OK)
                .extract().as(Comment[].class);

        Assert.assertEquals(commentsAfterParameterizedRequest, comments, "Comments list from equivalent paths responses are not the same");
    }

    @Test
    public void verifyCommentsIdsTest() {
        //the test verifies a comment id matches serial number
        final Comment[] comments = restClient.buildRequest()
                .param("postId", 1)
                .get(PATH_TO_COMMENTS)
                .then()
                .statusCode(SC_OK)
                .extract().as(Comment[].class);

        for (int i = 0; i < comments.length; i++) {
            Assert.assertEquals(comments[i].getId(), ++i);
        }
    }

    @Test(dataProvider = "commentProvider", dataProviderClass = CommentDataProvider.class)
    public void verifyIdempotenceTest(long postId, long commentId, int statusCode, Comment expectedComment) {
        //the test verifies there is no state change in the server (idempotence)
        //and after some the equivalent requests the server returns the expected object
        final int numberOfRequests = 2;
        executeNumberOfGetRequestsAndVerifyResponseData(numberOfRequests, postId, commentId, statusCode, expectedComment);
    }

    @Test
    public void verifyHeadersTest() {
        Headers headers = restClient.buildRequest()
                .header("Connection", "keep-alive")
                .header("Content-Type", "application/json; charset=utf-8")
                .param("postId", 1)
                .get(PATH_TO_COMMENTS)
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

    private void executeNumberOfGetRequestsAndVerifyResponseData(int numberOfRequests, long postId, long commentId, int statusCode, Comment expectedComment) {
        IntStream.range(0, numberOfRequests).forEach(request ->
                {
                    final Comment actualComment = restClient.buildRequest()
                            .param("postId", postId)
                            .param("id", commentId)
                            .get(PATH_TO_COMMENTS)
                            .then()
                            .statusCode(statusCode)
                            .extract().jsonPath()
                            .getObject("[0]", Comment.class);

                    Assert.assertEquals(actualComment, expectedComment);
                }
        );
    }
}
