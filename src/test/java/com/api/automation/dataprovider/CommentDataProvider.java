package com.api.automation.dataprovider;

import com.api.automation.model.Comment;

import static org.apache.http.HttpStatus.*;

public class CommentDataProvider {
    @org.testng.annotations.DataProvider(name = "commentProvider")
    public static Object[][] commentProvider() {
        final long postId = 1, id = 1;
        final String
                name = "id labore ex et quam laborum",
                email = "Eliseo@gardner.biz",
                body = "laudantium enim quasi est quidem magnam voluptate ipsam eos\ntempora quo necessitatibus\ndolor quam autem quasi\nreiciendis et nam sapiente accusantium";
        final Comment expectedComment = buildComment(postId, id, name, email, body);
        return new Object[][]{
                {postId, id, SC_OK, expectedComment}
        };
    }


    private static Comment buildComment(Long postId, Long id, String name, String email, String body) {
        return Comment.builder()
                .postId(postId)
                .id(id)
                .name(name)
                .email(email)
                .body(body)
                .build();
    }
}
