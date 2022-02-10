package blog.server;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.proto.blog.BlogId;
import com.proto.blog.BlogServiceGrpc;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import utils.ServerTestBase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class BlogDeleteTest extends ServerTestBase<
        BlogServiceGrpc.BlogServiceBlockingStub,
        BlogServiceGrpc.BlogServiceStub
> {
    @Mock
    private MongoClient mockClient;
    @Mock
    private MongoCollection<Document> mockCollection;
    @Mock
    private MongoDatabase mockDB;

    BlogDeleteTest() {
        MockitoAnnotations.openMocks(this);
        when(mockClient.getDatabase(anyString())).thenReturn(mockDB);
        when(mockDB.getCollection(anyString())).thenReturn(mockCollection);
        addService(new BlogServiceImpl(mockClient));
        setBlockingStubInstantiator(BlogServiceGrpc::newBlockingStub);
    }

    @Test
    void deleteTest() {
        String id = "579397d20c2dd41b9a8a09eb";

        when(mockCollection.deleteOne(any(Bson.class))).thenReturn(DeleteResult.acknowledged(1));

        BlogId b = blockingStub.deleteBlog(
            BlogId.newBuilder().setId(id).build()
        );

        assertEquals(id, b.getId());
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void deleteNotFoundTest() {
        String id = "579397d20c2dd41b9a8a09eb";

        when(mockCollection.deleteOne(any(Bson.class))).thenReturn(DeleteResult.unacknowledged());

        try {
            blockingStub.deleteBlog(BlogId.newBuilder().setId(id).build());
            fail("There should be an error in this case");
        } catch (StatusRuntimeException e) {
            Status status = Status.fromThrowable(e);

            assertEquals(Status.Code.NOT_FOUND, status.getCode());
            assertEquals(BlogServiceImpl.BLOG_WAS_NOT_FOUND, status.getDescription());
        }
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void deleteErrorTest() {
        String id = "579397d20c2dd41b9a8a09eb";

        when(mockCollection.deleteOne(any(Bson.class))).thenThrow(Status.UNKNOWN.asRuntimeException());

        try {
            blockingStub.deleteBlog(BlogId.newBuilder().setId(id).build());
            fail("There should be an error in this case");
        } catch (StatusRuntimeException e) {
            Status status = Status.fromThrowable(e);

            assertEquals(Status.Code.NOT_FOUND, status.getCode());
            assertNotNull(status.getDescription());
            assertTrue(status.getDescription().startsWith(BlogServiceImpl.BLOG_WAS_NOT_FOUND));
        }
    }
}
