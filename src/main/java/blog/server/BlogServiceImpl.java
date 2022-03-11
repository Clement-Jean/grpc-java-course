package blog.server;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.Empty;
import com.mongodb.client.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import com.proto.blog.Blog;
import com.proto.blog.BlogId;
import com.proto.blog.BlogServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.bson.Document;
import org.bson.types.ObjectId;

import static com.mongodb.client.model.Filters.eq;

public final class BlogServiceImpl extends BlogServiceGrpc.BlogServiceImplBase {

    @VisibleForTesting
    static final String BLOG_COULDNT_BE_CREATED = "The blog could not be created";
    @VisibleForTesting
    static final String BLOG_COULDNT_BE_UPDATED = "The blog could not be updated";
    @VisibleForTesting
    static final String BLOG_COULDNT_BE_DELETED = "The blog could not be deleted";
    @VisibleForTesting
    static final String BLOG_WAS_NOT_FOUND = "The blog with the corresponding id was not found";

    private final MongoCollection<Document> mongoCollection;

    BlogServiceImpl(MongoClient client) {
        MongoDatabase db = client.getDatabase("blogdb");
        mongoCollection = db.getCollection("blog");
    }

    private io.grpc.StatusRuntimeException error(Status status, String message) {
        return status.withDescription(message).asRuntimeException();
    }

    @SuppressWarnings("SameParameterValue")
    private io.grpc.StatusRuntimeException error(Status status, String message, String augmentMessage) {
        return status.withDescription(message)
            .augmentDescription(augmentMessage)
            .asRuntimeException();
    }

    Blog documentToBlog(Document document) {
        return Blog.newBuilder()
            .setAuthor(document.getString("author"))
            .setTitle(document.getString("title"))
            .setContent(document.getString("content"))
            .setId(document.getObjectId("_id").toString())
            .build();
    }

    @Override
    public void createBlog(Blog request, StreamObserver<BlogId> responseObserver) {
        System.out.println("Received Create Blog request");

        Document doc = new Document("author", request.getAuthor())
            .append("title", request.getTitle())
            .append("content", request.getContent());

        System.out.println("Inserting blog...");
        InsertOneResult result = mongoCollection.insertOne(doc);

        if (!result.wasAcknowledged() || result.getInsertedId() == null) {
            responseObserver.onError(error(Status.INTERNAL, BLOG_COULDNT_BE_CREATED));
            return;
        }

        String id = result.getInsertedId().asObjectId().getValue().toString();
        System.out.println("Inserted blog: " + id);

        responseObserver.onNext(BlogId.newBuilder().setId(id).build());
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
            responseObserver.onError(error(Status.NOT_FOUND, BLOG_WAS_NOT_FOUND, e.getLocalizedMessage()));
            return;
        }

        if (result == null) {
            System.out.println("Blog not found");
            responseObserver.onError(error(Status.NOT_FOUND, BLOG_WAS_NOT_FOUND));
            return;
        }

        System.out.println("Blog found, sending response");
        responseObserver.onNext(documentToBlog(result));
        responseObserver.onCompleted();
    }

    @Override
    public void updateBlog(Blog request, StreamObserver<Empty> responseObserver) {
        System.out.println("Received Update Blog request");
        String blogId = request.getId();

        System.out.println("Searching for a blog so we can update it");
        Document result;

        try {
            result = mongoCollection.find(eq("_id", new ObjectId(blogId))).first();
        } catch (Exception e) {
            responseObserver.onError(error(Status.NOT_FOUND, BLOG_WAS_NOT_FOUND, e.getLocalizedMessage()));
            return;
        }

        if (result == null) {
            System.out.println("Blog not found");
            responseObserver.onError(error(Status.NOT_FOUND, BLOG_WAS_NOT_FOUND));
            return;
        }

        Document replacement = new Document("author", request.getAuthor())
            .append("title", request.getTitle())
            .append("content", request.getContent())
            .append("_id", new ObjectId(blogId));

        System.out.println("Replacing blog in database...");

        UpdateResult updateResult = mongoCollection.replaceOne(eq("_id", result.getObjectId("_id")), replacement);

        if (!updateResult.wasAcknowledged()) {
            responseObserver.onError(error(Status.INTERNAL, BLOG_COULDNT_BE_UPDATED));
            return;
        }

        System.out.println("Replaced! Sending as a response");
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void deleteBlog(BlogId request, StreamObserver<Empty> responseObserver) {
        System.out.println("Received Delete Blog Request");

        DeleteResult result;

        try {
            result = mongoCollection.deleteOne(eq("_id", new ObjectId(request.getId())));
        } catch (Exception e) {
            System.out.println("Blog not found");
            responseObserver.onError(error(Status.NOT_FOUND, BLOG_WAS_NOT_FOUND, e.getLocalizedMessage()));
            return;
        }

        if (!result.wasAcknowledged()) {
            System.out.println("Blog could not be deleted");
            responseObserver.onError(error(Status.INTERNAL, BLOG_COULDNT_BE_DELETED));
            return;
        }

        if (result.getDeletedCount() == 0) {
            System.out.println("Blog not found");
            responseObserver.onError(error(Status.NOT_FOUND, BLOG_WAS_NOT_FOUND));
            return;
        }

        System.out.println("Blog was deleted");
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void listBlogs(Empty request, StreamObserver<Blog> responseObserver) {
        System.out.println("Received List Blog Request");

        for (Document document : mongoCollection.find()) {
            responseObserver.onNext(documentToBlog(document));
        }

        responseObserver.onCompleted();
    }
}
