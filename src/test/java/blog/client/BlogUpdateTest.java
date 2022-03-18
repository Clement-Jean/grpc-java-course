package blog.client;

import com.google.protobuf.Empty;
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

public class BlogUpdateTest extends ClientTestBase<BlogServiceGrpc.BlogServiceBlockingStub> {

    private static class UpdateSuccessBlogServer extends BlogServiceGrpc.BlogServiceImplBase {

        @Override
        public void updateBlog(Blog request, StreamObserver<Empty> responseObserver) {
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        }
    }

    private static class UpdateFailBlogServer extends BlogServiceGrpc.BlogServiceImplBase {
        @Override
        public void updateBlog(Blog request, StreamObserver<Empty> responseObserver) {
            responseObserver.onError(Status.NOT_FOUND.asRuntimeException());
        }
    }

    private final BlogId blogId = BlogId.newBuilder().setId("this_is_an_id").build();

    @Test
    void updateTest() throws IOException {
        createServerWithService(new UpdateSuccessBlogServer(), BlogServiceGrpc::newBlockingStub);

        assertNotNull(BlogClient.updateBlog(getStub(), blogId));
    }

    @Test
    void updateFailTest() throws IOException {
        createServerWithService(new UpdateFailBlogServer(), BlogServiceGrpc::newBlockingStub);

        assertNull(BlogClient.updateBlog(getStub(), blogId));
    }
}
