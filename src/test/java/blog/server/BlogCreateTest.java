package blog.server;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertOneResult;
import com.proto.blog.Blog;
import com.proto.blog.BlogId;
import com.proto.blog.BlogServiceGrpc;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.bson.BsonObjectId;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import utils.ServerTestBase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class BlogCreateTest extends ServerTestBase<
        BlogServiceGrpc.BlogServiceBlockingStub,
        BlogServiceGrpc.BlogServiceStub
> {

    @Mock
    private MongoClient mockClient;
    @Mock
    private MongoCollection<Document> mockCollection;
    @Mock
    private MongoDatabase mockDB;

    BlogCreateTest() {
        MockitoAnnotations.openMocks(this);
        when(mockClient.getDatabase(anyString())).thenReturn(mockDB);
        when(mockDB.getCollection(anyString())).thenReturn(mockCollection);
        addService(new BlogServiceImpl(mockClient));
        setBlockingStubInstantiator(BlogServiceGrpc::newBlockingStub);
    }

    @Test
    void createTest() {
        ObjectId id = new ObjectId("579397d20c2dd41b9a8a09eb");
        String author = "Clement";
        String title = "My Blog";
        String content = "This is a cool blog";

        Document blog = new Document()
                .append("author_id", author)
                .append("title", title)
                .append("content", content);

        when(mockCollection.insertOne(blog)).thenReturn(InsertOneResult.acknowledged(new BsonObjectId(id)));

        BlogId blogId = blockingStub.createBlog(
            Blog.newBuilder().setAuthorId(author)
                    .setTitle(title)
                    .setContent(content)
                    .build()
        );

        assertEquals(id.toString(), blogId.getId());
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void createFailTest() {
        String author = "Clement";
        String title = "My Blog";
        String content = "This is a cool blog";

        Document blog = new Document()
                .append("author_id", author)
                .append("title", title)
                .append("content", content);

        when(mockCollection.insertOne(blog)).thenReturn(InsertOneResult.unacknowledged());

        try {
            blockingStub.createBlog(
                Blog.newBuilder().setAuthorId(author)
                        .setTitle(title)
                        .setContent(content)
                        .build()
            );
            fail("There should be an error in this case");
        } catch (StatusRuntimeException e) {
            Status status = Status.fromThrowable(e);

            assertEquals(Status.Code.ABORTED, status.getCode());
            assertEquals(BlogServiceImpl.BLOG_COULDNT_BE_CREATED, status.getDescription());
        }
    }
}
