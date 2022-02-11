package blog.client;

import blog.server.BlogServer;
import blog.server.BlogServiceImpl;
import com.proto.blog.Blog;
import com.proto.blog.BlogId;
import com.proto.blog.BlogServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class BlogClient {
    private static void run(ManagedChannel channel) {
        BlogServiceGrpc.BlogServiceBlockingStub blogClient = BlogServiceGrpc.newBlockingStub(channel);
        BlogId createResponse = blogClient.createBlog(
                Blog.newBuilder()
                    .setAuthorId("Clement")
                    .setTitle("New blog!")
                    .setContent("Hello world this is my first blog!")
                    .build()
        );

        System.out.println("Received create blog response");
        System.out.println(createResponse.getId());

        System.out.println("Reading blog....");

        Blog readBlogResponse = blogClient.readBlog(createResponse);

        System.out.println(readBlogResponse.toString());

        Blog newBlog = Blog.newBuilder()
                .setId(createResponse.getId())
                .setAuthorId("Changed Author")
                .setTitle("New blog (updated)!")
                .setContent("Hello world this is my first blog! I've added some more content")
                .build();

        System.out.println("Updating blog...");
        Blog updateBlogResponse = blogClient.updateBlog(newBlog);

        System.out.println("Updated blog");
        System.out.println(updateBlogResponse.toString());

        blogClient.listBlog(com.google.protobuf.Empty.getDefaultInstance()).forEachRemaining(blog ->
            System.out.println(blog.toString())
        );

        System.out.println("Deleting blog");
        BlogId deleteBlogResponse = blogClient.deleteBlog(createResponse);

        System.out.println("Deleted blog: " + deleteBlogResponse.getId());
    }

    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051).usePlaintext().build();

        run(channel);

        System.out.println("Shutting Down");
        channel.shutdown();
    }
}
