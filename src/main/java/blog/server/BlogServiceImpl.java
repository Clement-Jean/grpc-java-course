package blog.server;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.Empty;
import com.mongodb.MongoException;
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
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

public final class BlogServiceImpl extends BlogServiceGrpc.BlogServiceImplBase {

    @VisibleForTesting
    static final String BLOG_COULDNT_BE_CREATED = "The blog could not be created";
    @VisibleForTesting
    static final String BLOG_COULDNT_BE_DELETED = "The blog could not be deleted";
    @VisibleForTesting
    static final String BLOG_WAS_NOT_FOUND = "The blog with the corresponding id was not found";
    @VisibleForTesting
    static final String ID_CANNOT_BE_EMPTY = "The blog ID cannot be empty";

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
        InsertOneResult result;

        try {
            result = mongoCollection.insertOne(doc);
        } catch (MongoException e) {
            responseObserver.onError(error(Status.INTERNAL, BLOG_COULDNT_BE_CREATED, e.getLocalizedMessage()));
            return;
        }

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

        if (request.getId().isEmpty()) {
            responseObserver.onError(error(Status.INVALID_ARGUMENT, ID_CANNOT_BE_EMPTY));
            return;
        }

        String id = request.getId();

        System.out.println("Searching for a blog with id: " + id);
        Document result = mongoCollection.find(eq("_id", new ObjectId(id))).first();

        if (result == null) {
            System.out.println("Blog not found");
            responseObserver.onError(
                error(
                    Status.NOT_FOUND,
                    BLOG_WAS_NOT_FOUND,
                    "BlogId: " + id
                )
            );
            return;
        }

        System.out.println("Blog found, sending response");
        responseObserver.onNext(documentToBlog(result));
        responseObserver.onCompleted();
    }

    @Override
    public void updateBlog(Blog request, StreamObserver<Empty> responseObserver) {
        System.out.println("Received Update Blog request");

        if (request.getId().isEmpty()) {
            responseObserver.onError(error(Status.INVALID_ARGUMENT, ID_CANNOT_BE_EMPTY));
            return;
        }

        String id = request.getId();

        System.out.println("Searching for a blog so we can update it");
        Document result = mongoCollection.findOneAndUpdate(
            eq("_id", new ObjectId(id)),
            combine(
                set("author", request.getAuthor()),
                set("title", request.getTitle()),
                set("content", request.getContent())
            )
        );

        if (result == null) {
            System.out.println("Blog not found");
            responseObserver.onError(
                error(
                    Status.NOT_FOUND,
                    BLOG_WAS_NOT_FOUND,
                    "BlogId: " + id
                )
            );
            return;
        }

        System.out.println("Replaced! Sending as a response");
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void deleteBlog(BlogId request, StreamObserver<Empty> responseObserver) {
        System.out.println("Received Delete Blog Request");

        if (request.getId().isEmpty()) {
            responseObserver.onError(error(Status.INVALID_ARGUMENT, ID_CANNOT_BE_EMPTY));
            return;
        }

        String id = request.getId();
        DeleteResult result;

        try {
            result = mongoCollection.deleteOne(eq("_id", new ObjectId(id)));
        } catch (MongoException e) {
            responseObserver.onError(error(Status.INTERNAL, BLOG_COULDNT_BE_DELETED, e.getLocalizedMessage()));
            return;
        }

        if (!result.wasAcknowledged()) {
            System.out.println("Blog could not be deleted");
            responseObserver.onError(error(Status.INTERNAL, BLOG_COULDNT_BE_DELETED));
            return;
        }

        if (result.getDeletedCount() == 0) {
            System.out.println("Blog not found");
            responseObserver.onError(
                error(
                    Status.NOT_FOUND,
                    BLOG_WAS_NOT_FOUND,
                    "BlogId: " + id
                )
            );
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
