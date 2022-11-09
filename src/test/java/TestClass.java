import com.rest_assured.client.RestClient;
import com.rest_assured.pojo.Post;
import io.restassured.response.ValidatableResponse;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.apache.http.HttpStatus.*;

@SpringBootTest(classes = RestClient.class)
public class TestClass extends BaseTest {
    private final static int
            USER_ID = 1,
            POST_ID = 9,
            NOT_EXISTING_POST_ID = 999;
    private final static String
            TITLE = "nesciunt iure omnis dolorem tempora et accusantium",
            BODY = "consectetur animi nesciunt iure dolore\nenim quia ad\nveniam autem ut quam aut nobis\net est aut quod aut provident voluptas autem voluptas";
    private final static Post EXPECTED_POST = getExpectedPost(USER_ID, POST_ID, TITLE, BODY);
    private final static Post NOT_EXISTING_POST = getExpectedPost(null, null, null, null);

    @DataProvider(name = "getPostProvider")
    public static Object[][] dataProvider() {
        return new Object[][]{
                {POST_ID, SC_OK, EXPECTED_POST},
                {NOT_EXISTING_POST_ID, SC_NOT_FOUND, NOT_EXISTING_POST},
        };
    }

    @Test(dataProvider = "getPostProvider")
    public void postTest(int id, int statusCode, Post expectedPost) {
        final ValidatableResponse response = restClient.get("/posts/" + id, statusCode);
        final Post actualPost = restClient.extract(response, Post.class);
        Assert.assertEquals(actualPost, expectedPost, "Wrong post");
    }

    private static Post getExpectedPost(Integer userId, Integer id, String title, String body) {
        return Post.builder()
                .userId(userId)
                .id(id)
                .title(title)
                .body(body)
                .build();
    }
}
