package blog.server;

import com.mongodb.client.*;
import com.mongodb.client.result.UpdateResult;
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
                .append("author_id", author + "_old")
                .append("title", title + "_old")
                .append("content", content + "_old");

        when(mockCollection.find(any(Bson.class))).thenReturn(iterable);
        when(iterable.first()).thenReturn(blog);
        when(mockCollection.replaceOne(any(Bson.class), any(Document.class)))
                .thenReturn(UpdateResult.acknowledged(1, null, null));

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
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void updateNotAcknowledgedTest() {
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
        when(mockCollection.replaceOne(any(Bson.class), any(Document.class))).thenReturn(UpdateResult.unacknowledged());

        try {
            blockingStub.updateBlog(Blog.newBuilder().setId(id).build());
            fail("There should be an error in this case");
        } catch (StatusRuntimeException e) {
            Status status = Status.fromThrowable(e);

            assertEquals(Status.Code.INTERNAL, status.getCode());
            assertEquals(BlogServiceImpl.BLOG_COULDNT_BE_UPDATED, status.getDescription());
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
