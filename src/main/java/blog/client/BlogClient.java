package blog.client;

import com.proto.blog.Blog;
import com.proto.blog.BlogId;
import com.proto.blog.BlogServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class BlogClient {
    private static void run(ManagedChannel channel) {
        BlogServiceGrpc.BlogServiceBlockingStub blogClient = BlogServiceGrpc.newBlockingStub(channel);

        System.out.println("Creating blog....");
        BlogId createResponse = blogClient.createBlog(
                Blog.newBuilder()
                    .setAuthorId("Clement")
                    .setTitle("New blog!")
                    .setContent("Hello world this is my first blog!")
                    .build()
        );

        System.out.println("Blog created: " + createResponse.getId());
        System.out.println();

        System.out.println("Reading blog....");

        Blog readBlogResponse = blogClient.readBlog(createResponse);

        System.out.println("Blog read:");
        System.out.println(readBlogResponse.toString());

        Blog newBlog = Blog.newBuilder()
                .setId(createResponse.getId())
                .setAuthorId("Changed Author")
                .setTitle("New blog (updated)!")
                .setContent("Hello world this is my first blog! I've added some more content")
                .build();

        System.out.println("Updating blog...");
        Blog updateBlogResponse = blogClient.updateBlog(newBlog);

        System.out.println("Blog updated:");
        System.out.println(updateBlogResponse.toString());

        System.out.println("Listing blogs...");
        blogClient.listBlog(com.google.protobuf.Empty.getDefaultInstance()).forEachRemaining(blog ->
            System.out.println(blog.toString())
        );

        System.out.println("Deleting blog");
        BlogId deleteBlogResponse = blogClient.deleteBlog(createResponse);

        System.out.println("Blog deleted: " + deleteBlogResponse.getId());
    }

    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051).usePlaintext().build();

        run(channel);

        System.out.println("Shutting Down");
        channel.shutdown();
    }
}
