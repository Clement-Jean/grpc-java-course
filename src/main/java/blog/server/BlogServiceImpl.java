package blog.server;

import com.google.protobuf.Empty;
import com.mongodb.client.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.proto.blog.Blog;
import com.proto.blog.BlogId;
import com.proto.blog.BlogServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.bson.Document;
import org.bson.types.ObjectId;

import static com.mongodb.client.model.Filters.eq;

public class BlogServiceImpl extends BlogServiceGrpc.BlogServiceImplBase {

    public static final String BLOG_COULDNT_BE_CREATED = "The blog could not be created";
    public static final String BLOG_WAS_NOT_FOUND = "The blog with the corresponding id was not found";

    private final MongoCollection<Document> mongoCollection;

    BlogServiceImpl(MongoClient mongoClient) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase("blogdb");
        this.mongoCollection = mongoDatabase.getCollection("blog");
    }

    Blog documentToBlog(Document document){
        return Blog.newBuilder()
                .setAuthorId(document.getString("author_id"))
                .setTitle(document.getString("title"))
                .setContent(document.getString("content"))
                .setId(document.getObjectId("_id").toString())
                .build();
    }

    @Override
    public void createBlog(Blog request, StreamObserver<BlogId> responseObserver) {
        System.out.println("Received Create Blog request");

        Document doc = new Document("author_id", request.getAuthorId())
                .append("title", request.getTitle())
                .append("content", request.getContent());

        System.out.println("Inserting blog...");
        InsertOneResult result = mongoCollection.insertOne(doc);

        if (!result.wasAcknowledged() || result.getInsertedId() == null) {
            responseObserver.onError(
                Status.ABORTED
                    .withDescription(BLOG_COULDNT_BE_CREATED)
                    .asRuntimeException()
            );
            return;
        }

        String id = result.getInsertedId().asObjectId().getValue().toString();
        System.out.println("Inserted blog: " + id);

        BlogId response = BlogId.newBuilder().setId(id).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void readBlog(BlogId request, StreamObserver<Blog> responseObserver) {
        System.out.println("Received Read Blog request");

        String blogId = request.getId();

        System.out.println("Searching for a blog with id: " + blogId);
        Document result;

        try {
            result = mongoCollection.find(eq("_id", new ObjectId(blogId))).first();
        } catch (Exception e) {
            responseObserver.onError(
                Status.NOT_FOUND
                    .withDescription(BLOG_WAS_NOT_FOUND)
                    .augmentDescription(e.getLocalizedMessage())
                    .asRuntimeException()
            );
            return;
        }

        if (result == null) {
            System.out.println("Blog not found");
            responseObserver.onError(
                Status.NOT_FOUND
                    .withDescription(BLOG_WAS_NOT_FOUND)
                    .asRuntimeException()
            );
            return;
        }

        System.out.println("Blog found, sending response");
        responseObserver.onNext(documentToBlog(result));
        responseObserver.onCompleted();
    }

    @Override
    public void updateBlog(Blog request, StreamObserver<Blog> responseObserver) {
        System.out.println("Received Update Blog request");
        String blogId = request.getId();

        System.out.println("Searching for a blog so we can update it");
        Document result;

        try {
            result = mongoCollection.find(eq("_id", new ObjectId(blogId))).first();
        } catch (Exception e) {
            responseObserver.onError(
                Status.NOT_FOUND
                    .withDescription(BLOG_WAS_NOT_FOUND)
                    .augmentDescription(e.getLocalizedMessage())
                    .asRuntimeException()
            );
            return;
        }

        if (result == null) {
            System.out.println("Blog not found");
            responseObserver.onError(
                Status.NOT_FOUND
                    .withDescription(BLOG_WAS_NOT_FOUND)
                    .asRuntimeException()
            );
            return;
        }

        Document replacement = new Document("author_id", request.getAuthorId())
                .append("title", request.getTitle())
                .append("content", request.getContent())
                .append("_id", new ObjectId(blogId));

        System.out.println("Replacing blog in database...");

        mongoCollection.replaceOne(eq("_id", result.getObjectId("_id")), replacement);

        System.out.println("Replaced! Sending as a response");
        responseObserver.onNext(documentToBlog(replacement));
        responseObserver.onCompleted();
    }

    @Override
    public void deleteBlog(BlogId request, StreamObserver<BlogId> responseObserver) {
        System.out.println("Received Delete Blog Request");

        DeleteResult result;

        try {
            result = mongoCollection.deleteOne(eq("_id", new ObjectId(request.getId())));
        } catch (Exception e) {
            System.out.println("Blog not found");
            responseObserver.onError(
                Status.NOT_FOUND
                    .withDescription(BLOG_WAS_NOT_FOUND)
                    .augmentDescription(e.getLocalizedMessage())
                    .asRuntimeException()
            );
            return;
        }

        if (!result.wasAcknowledged() || result.getDeletedCount() == 0) {
            System.out.println("Blog not found");
            responseObserver.onError(
                Status.NOT_FOUND
                    .withDescription(BLOG_WAS_NOT_FOUND)
                    .asRuntimeException()
            );
            return;
        }

        System.out.println("Blog was deleted");
        responseObserver.onNext(request);
        responseObserver.onCompleted();
    }

    @Override
    public void listBlog(Empty request, StreamObserver<Blog> responseObserver) {
        System.out.println("Received List Blog Request");

        for (Document document : mongoCollection.find()) {
            responseObserver.onNext(documentToBlog(document));
        }

        responseObserver.onCompleted();
    }
}
