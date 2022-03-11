package blog.client;

import com.google.protobuf.Empty;
import com.proto.blog.BlogId;
import com.proto.blog.BlogServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import utils.ClientTestBase;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class BlogDeleteTest extends ClientTestBase<BlogServiceGrpc.BlogServiceBlockingStub> {

    private static class DeleteSuccessBlogServer extends BlogServiceGrpc.BlogServiceImplBase {
        @Override
        public void deleteBlog(BlogId request, StreamObserver<Empty> responseObserver) {
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        }
    }

    private static class DeleteFailBlogServer extends BlogServiceGrpc.BlogServiceImplBase {
        @Override
        public void deleteBlog(BlogId request, StreamObserver<Empty> responseObserver) {
            responseObserver.onError(Status.NOT_FOUND.asRuntimeException());
        }
    }

    private final BlogId fakeBlogId = BlogId.newBuilder().setId("this_is_an_id").build();

    @Test
    void deleteTest() throws IOException {
        createServerWithService(new DeleteSuccessBlogServer(), BlogServiceGrpc::newBlockingStub);

        assertNotNull(BlogClient.deleteBlog(getStub(), fakeBlogId));
    }

    @Test
    void deleteFailTest() throws IOException {
        createServerWithService(new DeleteFailBlogServer(), BlogServiceGrpc::newBlockingStub);

        assertNull(BlogClient.deleteBlog(getStub(), fakeBlogId));
    }
}
