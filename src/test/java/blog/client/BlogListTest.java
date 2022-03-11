package blog.client;

import com.google.protobuf.Empty;
import com.proto.blog.Blog;
import com.proto.blog.BlogServiceGrpc;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import utils.ClientTestBase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BlogListTest extends ClientTestBase<BlogServiceGrpc.BlogServiceBlockingStub> {

    private static class ListEmptyBlogServer extends BlogServiceGrpc.BlogServiceImplBase {
        @Override
        public void listBlogs(Empty request, StreamObserver<Blog> responseObserver) {
            responseObserver.onCompleted();
        }
    }

    private static class ListNonEmptyBlogServer extends BlogServiceGrpc.BlogServiceImplBase {
        @Override
        public void listBlogs(Empty request, StreamObserver<Blog> responseObserver) {
            responseObserver.onNext(Blog.newBuilder()
                .setId("id")
                .setAuthor("Clement")
                .setTitle("Title")
                .setContent("Content")
                .build()
            );
            responseObserver.onCompleted();
        }
    }

    @Test
    void listEmptyTest() throws IOException {
        createServerWithService(new ListEmptyBlogServer(), BlogServiceGrpc::newBlockingStub);
        ByteArrayOutputStream outSpy = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(outSpy);

        BlogClient.listBlogs(getStub(), ps);
        assertEquals("Listing blogs...\n", outSpy.toString());
    }

    @Test
    void listNonEmptyTest() throws IOException {
        createServerWithService(new ListNonEmptyBlogServer(), BlogServiceGrpc::newBlockingStub);
        ByteArrayOutputStream outSpy = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(outSpy);

        BlogClient.listBlogs(getStub(), ps);
        assertEquals(
            "Listing blogs...\n" +
            "id: \"id\"\n" +
            "author: \"Clement\"\n" +
            "title: \"Title\"\n" +
            "content: \"Content\"\n",
            outSpy.toString()
        );
    }
}
