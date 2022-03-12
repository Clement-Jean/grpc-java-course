package blog.server;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.proto.blog.Blog;
import com.proto.blog.BlogServiceGrpc;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import utils.ServerTestBase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BlogUpdateTest extends ServerTestBase<
        BlogServiceGrpc.BlogServiceBlockingStub,
        BlogServiceGrpc.BlogServiceStub
> {

    @Mock
    private MongoClient mockClient;
    @Mock
    private MongoCollection<Document> mockCollection;
    @Mock
    private MongoDatabase mockDB;
    @Mock
    private FindIterable<Document> iterable;

    BlogUpdateTest() {
        MockitoAnnotations.openMocks(this);
        when(mockClient.getDatabase(anyString())).thenReturn(mockDB);
        when(mockDB.getCollection(anyString())).thenReturn(mockCollection);
        addService(new BlogServiceImpl(mockClient));
        setBlockingStubInstantiator(BlogServiceGrpc::newBlockingStub);
    }

    @Test
    void updateTest() {
        String id = "579397d20c2dd41b9a8a09eb";
        ObjectId oid = new ObjectId(id);
        String author = "Clement";
        String title = "My Blog";
        String content = "This is a cool blog";
        Document blog = new Document("_id", oid)
                .append("author", author + "_old")
                .append("title", title + "_old")
                .append("content", content + "_old");

        when(mockCollection.findOneAndUpdate(any(Bson.class), any(Bson.class))).thenReturn(blog);

        assertDoesNotThrow(() -> blockingStub.updateBlog(
                Blog.newBuilder()
                        .setId(id)
                        .setTitle(title)
                        .setAuthor(author)
                        .setContent(content)
                        .build()
        ));
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void readInvalidIdTest() {
        try {
            blockingStub.updateBlog(Blog.newBuilder().build());
            fail("There should be an error in this case");
        } catch (StatusRuntimeException e) {
            Status status = Status.fromThrowable(e);

            assertEquals(Status.Code.INVALID_ARGUMENT, status.getCode());
            assertEquals(BlogServiceImpl.ID_CANNOT_BE_EMPTY, status.getDescription());
        }
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void updateNotFoundTest() {
        String id = "579397d20c2dd41b9a8a09eb";

        when(mockCollection.find(any(Bson.class))).thenReturn(iterable);
        when(iterable.first()).thenReturn(null);

        try {
            blockingStub.updateBlog(Blog.newBuilder().setId(id).build());
            fail("There should be an error in this case");
        } catch (StatusRuntimeException e) {
            Status status = Status.fromThrowable(e);

            assertEquals(Status.Code.NOT_FOUND, status.getCode());
            assertNotNull(status.getDescription());
            assertTrue(status.getDescription().startsWith(BlogServiceImpl.BLOG_WAS_NOT_FOUND));
        }
    }
}
