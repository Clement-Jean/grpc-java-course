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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doReturn;

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

    BlogUpdateTest() {
        MockitoAnnotations.openMocks(this);
        when(mockClient.getDatabase(anyString())).thenReturn(mockDB);
        when(mockDB.getCollection(anyString())).thenReturn(mockCollection);
        addService(new BlogServiceImpl(mockClient));
        setBlockingStubInstantiator(BlogServiceGrpc::newBlockingStub);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void updateTest() {
        FindIterable iterable = mock(FindIterable.class);

        String id = "579397d20c2dd41b9a8a09eb";
        ObjectId oid = new ObjectId(id);
        String author = "Clement";
        String title = "My Blog";
        String content = "This is a cool blog";

        Document blog = new Document("_id", oid)
                .append("author_id", author + "_old")
                .append("title", title + "_old")
                .append("content", content + "_old");

        when(mockCollection.find(any(Bson.class))).thenReturn(iterable);
        when(iterable.first()).thenReturn(blog);

        Blog b = blockingStub.updateBlog(
            Blog.newBuilder()
                .setId(id)
                .setTitle(title)
                .setAuthorId(author)
                .setContent(content)
                .build()
        );

        assertEquals(id, b.getId());
        assertEquals(author, b.getAuthorId());
        assertEquals(title, b.getTitle());
        assertEquals(content, b.getContent());
    }

    @Test
    @SuppressWarnings({"rawtypes", "ResultOfMethodCallIgnored"})
    void updateNotFoundTest() {
        FindIterable iterable = mock(FindIterable.class);

        String id = "579397d20c2dd41b9a8a09eb";

        when(mockCollection.find(any(Bson.class))).thenReturn(iterable);
        when(iterable.first()).thenReturn(null);

        try {
            blockingStub.updateBlog(Blog.newBuilder().setId(id).build());
            fail("There should be an error in this case");
        } catch (StatusRuntimeException e) {
            Status status = Status.fromThrowable(e);

            assertEquals(Status.Code.NOT_FOUND, status.getCode());
            assertEquals(BlogServiceImpl.BLOG_WAS_NOT_FOUND, status.getDescription());
        }
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void updateErrorTest() {
        String id = "579397d20c2dd41b9a8a09eb";

        when(mockCollection.find(any(Bson.class))).thenThrow(Status.UNKNOWN.asRuntimeException());

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
