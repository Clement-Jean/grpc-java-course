package blog.server;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public final class BlogServer {
    public static void main(String[] args) throws InterruptedException, IOException {
        int port = 50051;

        MongoClient client = MongoClients.create("mongodb://root:root@localhost:27017/");

        Server server = ServerBuilder.forPort(port)
                .addService(new BlogServiceImpl(client))
                .build();

        server.start();
        System.out.println("Server Started");
        System.out.println("Listening on port: " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Received Shutdown Request");
            server.shutdown();
            client.close();
            System.out.println("Server Stopped");
        }));

        server.awaitTermination();
    }
}
