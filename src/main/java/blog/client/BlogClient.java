package blog.client;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.Empty;
import com.proto.blog.Blog;
import com.proto.blog.BlogId;
import com.proto.blog.BlogServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import utils.ExcludeFromJacocoGeneratedReport;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.PrintStream;

public final class BlogClient {

    private BlogClient() {}

    @Nullable
    @VisibleForTesting
    static BlogId createBlog(BlogServiceGrpc.BlogServiceBlockingStub stub) {
        System.out.println("Creating blog....");

        try {
            BlogId createResponse = stub.createBlog(
                Blog.newBuilder()
                    .setAuthor("Clement")
                    .setTitle("New blog!")
                    .setContent("Hello world this is my first blog!")
                    .build()
            );

            System.out.println("Blog created: " + createResponse.getId());
            System.out.println();
            return createResponse;
        } catch (StatusRuntimeException e) {
            System.out.println("Couldn't create the blog");
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    @VisibleForTesting
    static Blog readBlog(BlogServiceGrpc.BlogServiceBlockingStub stub, @Nonnull BlogId blogId) {
        System.out.println("Reading blog....");

        try {
            Blog readResponse = stub.readBlog(blogId);

            System.out.println("Blog read:" + readResponse);
            return readResponse;
        } catch (StatusRuntimeException e) {
            System.out.println("Couldn't read the blog");
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    @VisibleForTesting
    @SuppressWarnings("ResultOfMethodCallIgnored")
    static Blog updateBlog(BlogServiceGrpc.BlogServiceBlockingStub stub, @Nonnull BlogId blogId) {
        try {
            Blog newBlog = Blog.newBuilder()
                .setId(blogId.getId())
                .setAuthor("Changed Author")
                .setTitle("New blog (updated)!")
                .setContent("Hello world this is my first blog! I've added some more content")
                .build();

            System.out.println("Updating blog...");
            stub.updateBlog(newBlog);

            System.out.println("Blog updated:");
            System.out.println(newBlog);
            return newBlog;
        } catch (StatusRuntimeException e) {
            System.out.println("Couldn't update the blog");
            e.printStackTrace();
            return null;
        }
    }

    @VisibleForTesting
    static void listBlogs(BlogServiceGrpc.BlogServiceBlockingStub stub, PrintStream ps) {
        ps.println("Listing blogs...");
        stub.listBlogs(Empty.getDefaultInstance()).forEachRemaining(ps::print);
    }

    @Nullable
    @VisibleForTesting
    @SuppressWarnings("ResultOfMethodCallIgnored")
    static BlogId deleteBlog(BlogServiceGrpc.BlogServiceBlockingStub stub, @Nonnull BlogId blogId) {
        try {
            System.out.println("Deleting blog");
            stub.deleteBlog(blogId);

            System.out.println("Blog deleted: " + blogId.getId());
            return blogId;
        } catch (StatusRuntimeException e) {
            System.out.println("Couldn't delete the blog");
            e.printStackTrace();
            return null;
        }
    }

    @ExcludeFromJacocoGeneratedReport
    private static void run(ManagedChannel channel) {
        BlogServiceGrpc.BlogServiceBlockingStub stub = BlogServiceGrpc.newBlockingStub(channel);

        BlogId id = createBlog(stub);

        if (id == null)
            return;

        readBlog(stub, id);
        updateBlog(stub, id);
        listBlogs(stub, System.out);
        deleteBlog(stub, id);
    }

    @ExcludeFromJacocoGeneratedReport
    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
            .usePlaintext()
            .build();

        run(channel);

        System.out.println("Shutting Down");
        channel.shutdown();
    }
}
