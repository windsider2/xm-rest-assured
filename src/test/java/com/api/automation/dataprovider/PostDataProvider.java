package com.api.automation.dataprovider;

import com.api.automation.model.Post;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

import static java.lang.String.*;
import static org.apache.http.HttpStatus.*;

public class PostDataProvider {
    @org.testng.annotations.DataProvider(name = "postProvider")
    public static Object[][] postProvider() {
        final int userId = 1, postId = 9, notExistingPostId = 999;
        final String
                title = "nesciunt iure omnis dolorem tempora et accusantium",
                body = "consectetur animi nesciunt iure dolore\nenim quia ad\nveniam autem ut quam aut nobis\net est aut quod aut provident voluptas autem voluptas";
        final Post
                expectedPost = buildPost(userId, postId, title, body),
                notExistingPost = buildPost(null, null, null, null);
        return new Object[][]{
                {postId, SC_OK, expectedPost},
                {notExistingPostId, SC_NOT_FOUND, notExistingPost},
        };
    }

    @org.testng.annotations.DataProvider(name = "postProviderForIdempotence")
    public static Object[][] postProviderForIdempotence() {
        final int userId = 1, postId = 1;
        final String
                title = "sunt aut facere repellat provident occaecati excepturi optio reprehenderit",
                body = "quia et suscipit\nsuscipit recusandae consequuntur expedita et cum\nreprehenderit molestiae ut ut quas totam\nnostrum rerum est autem sunt rem eveniet architecto";
        final Post expectedPost = buildPost(userId, postId, title, body);
        return new Object[][]{
                {postId, SC_OK, expectedPost},
        };
    }

    @org.testng.annotations.DataProvider(name = "postBodyProvider")
    public static Object[][] postBodyProvider() {
        final int userId = 20, postId = 121;
        final String title = "Post id 101", body = "Text for body", expectedBody = "{\"id\":101}", emptyBody = "{}";
        final Post bodyToPost = buildPost(userId, postId, title, body);
        return new Object[][]{
                {"", bodyToPost, SC_CREATED, expectedBody},
                {"/1", bodyToPost, SC_NOT_FOUND, emptyBody}
        };
    }

    @org.testng.annotations.DataProvider(name = "putBodyProvider")
    public static Object[][] putBodyProvider() {
        final int userId = 1, postId = 1, notExistingPostId = 999;
        final String title = "Put id 1", body = "Text for body",
                expectedBody = format("\"id\": %s", userId),
                errorBody = "TypeError: Cannot read properties of undefined (reading 'id')";
        final String bodyToPost = generateSerializedPost(buildPost(userId, postId, title, body));
        return new Object[][]{
                {postId, bodyToPost, SC_OK, expectedBody},
                {notExistingPostId, bodyToPost, SC_INTERNAL_SERVER_ERROR, errorBody}

        };
    }

    private static Post buildPost(Integer userId, Integer id, String title, String body) {
        return Post.builder()
                .userId(userId)
                .id(id)
                .title(title)
                .body(body)
                .build();
    }

    public static String generateSerializedPost(Post post) {
        try {
            return new ObjectMapper().writeValueAsString(post);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
