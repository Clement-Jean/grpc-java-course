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

public class BlogCreateTest extends ClientTestBase<BlogServiceGrpc.BlogServiceBlockingStub> {
    private static class CreateSuccessBlogServer extends BlogServiceGrpc.BlogServiceImplBase {
        @Override
        public void createBlog(Blog request, StreamObserver<BlogId> responseObserver) {
            responseObserver.onNext(BlogId.newBuilder().setId("this_is_an_id").build());
            responseObserver.onCompleted();
        }
    }

    private static class CreateFailBlogServer extends BlogServiceGrpc.BlogServiceImplBase {
        @Override
        public void createBlog(Blog request, StreamObserver<BlogId> responseObserver) {
            responseObserver.onError(Status.INTERNAL.asRuntimeException());
        }
    }

    @Test
    void createTest() throws IOException {
        createServerWithService(new CreateSuccessBlogServer(), BlogServiceGrpc::newBlockingStub);

        assertNotNull(BlogClient.createBlog(getStub()));
    }

    @Test
    void createFailTest() throws IOException {
        createServerWithService(new CreateFailBlogServer(), BlogServiceGrpc::newBlockingStub);

        assertNull(BlogClient.createBlog(getStub()));
    }
}
