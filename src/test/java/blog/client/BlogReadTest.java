package blog.client;

import com.proto.blog.Blog;
import com.proto.blog.BlogId;
import com.proto.blog.BlogServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import utils.ClientTestBase;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class BlogReadTest extends ClientTestBase<BlogServiceGrpc.BlogServiceBlockingStub> {

    private static class ReadSuccessBlogServer extends BlogServiceGrpc.BlogServiceImplBase {

        @Override
        public void readBlog(BlogId request, StreamObserver<Blog> responseObserver) {
            responseObserver.onNext(Blog.getDefaultInstance());
            responseObserver.onCompleted();
        }
    }

    private static class ReadFailBlogServer extends BlogServiceGrpc.BlogServiceImplBase {
        @Override
        public void readBlog(BlogId request, StreamObserver<Blog> responseObserver) {
            responseObserver.onError(Status.NOT_FOUND.asRuntimeException());
        }
    }

    private final BlogId fakeBlogId = BlogId.newBuilder().setId("this_is_an_id").build();

    @Test
    void readTest() throws IOException {
        createServerWithService(new ReadSuccessBlogServer(), BlogServiceGrpc::newBlockingStub);

        assertNotNull(BlogClient.readBlog(getStub(), fakeBlogId));
    }

    @Test
    void deleteFailTest() throws IOException {
        createServerWithService(new ReadFailBlogServer(), BlogServiceGrpc::newBlockingStub);

        assertNull(BlogClient.readBlog(getStub(), fakeBlogId));
    }
}
