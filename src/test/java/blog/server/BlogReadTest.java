package blog.server;

import com.mongodb.client.*;
import com.proto.blog.Blog;
import com.proto.blog.BlogId;
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

public class BlogReadTest extends ServerTestBase<
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

    BlogReadTest() {
        MockitoAnnotations.openMocks(this);
        when(mockClient.getDatabase(anyString())).thenReturn(mockDB);
        when(mockDB.getCollection(anyString())).thenReturn(mockCollection);
        addService(new BlogServiceImpl(mockClient));
        setBlockingStubInstantiator(BlogServiceGrpc::newBlockingStub);
    }

    @Test
    void readTest() {
        String id = "579397d20c2dd41b9a8a09eb";
        ObjectId oid = new ObjectId(id);
        String author = "Clement";
        String title = "My Blog";
        String content = "This is a cool blog";

        Document blog = new Document("_id", oid)
                .append("author", author)
                .append("title", title)
                .append("content", content);

        when(mockCollection.find(any(Bson.class))).thenReturn(iterable);
        when(iterable.first()).thenReturn(blog);

        Blog b = blockingStub.readBlog(BlogId.newBuilder().setId(id).build());

        assertEquals(id, b.getId());
        assertEquals(author, b.getAuthor());
        assertEquals(title, b.getTitle());
        assertEquals(content, b.getContent());
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void readInvalidIdTest() {
        try {
            blockingStub.readBlog(BlogId.getDefaultInstance());
            fail("There should be an error in this case");
        } catch (StatusRuntimeException e) {
            Status status = Status.fromThrowable(e);

            assertEquals(Status.Code.INVALID_ARGUMENT, status.getCode());
            assertEquals(BlogServiceImpl.ID_CANNOT_BE_EMPTY, status.getDescription());
        }
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void readNotFoundTest() {
        String id = "579397d20c2dd41b9a8a09eb";

        when(mockCollection.find(any(Bson.class))).thenReturn(iterable);
        when(iterable.first()).thenReturn(null);

        try {
            blockingStub.readBlog(BlogId.newBuilder().setId(id).build());
            fail("There should be an error in this case");
        } catch (StatusRuntimeException e) {
            Status status = Status.fromThrowable(e);

            assertEquals(Status.Code.NOT_FOUND, status.getCode());
            assertNotNull(status.getDescription());
            assertTrue(status.getDescription().startsWith(BlogServiceImpl.BLOG_WAS_NOT_FOUND));
        }
    }
}
