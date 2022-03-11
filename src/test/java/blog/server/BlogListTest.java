package blog.server;

import com.mongodb.client.*;
import com.proto.blog.Blog;
import com.proto.blog.BlogServiceGrpc;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import utils.ServerTestBase;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class BlogListTest extends ServerTestBase<
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
    @Mock
    private MongoCursor<Document> cursor;

    private final List<Blog> finalResult = new ArrayList<>();

    BlogListTest() {
        MockitoAnnotations.openMocks(this);
        when(mockClient.getDatabase(anyString())).thenReturn(mockDB);
        when(mockDB.getCollection(anyString())).thenReturn(mockCollection);
        addService(new BlogServiceImpl(mockClient));
        setBlockingStubInstantiator(BlogServiceGrpc::newBlockingStub);
    }

    @Test
    void listTest() {
        String author = "Clement";
        String title = "My Blog";
        String content = "This is a cool blog";

        Document blog1 = new Document("_id", new ObjectId("579397d20c2dd41b9a8a09eb"))
                .append("author", author)
                .append("title", title)
                .append("content", content);
        Document blog2 = new Document("_id", new ObjectId("579397d20c2dd41b9a8a09ec"))
                .append("author", author + "2")
                .append("title", title + "2")
                .append("content", content + "2");

        when(mockCollection.find()).thenReturn(iterable);
        when(iterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true, true, false);
        when(cursor.next()).thenReturn(blog1, blog2);

        blockingStub.listBlogs(com.google.protobuf.Empty.getDefaultInstance()).forEachRemaining(finalResult::add);

        assertEquals(2, finalResult.size());
        assertEquals(author, finalResult.get(0).getAuthor());
        assertEquals(author + "2", finalResult.get(1).getAuthor());
    }
}
