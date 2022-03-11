package greeting.client;

import com.proto.greeting.GreetingRequest;
import com.proto.greeting.GreetingResponse;
import com.proto.greeting.GreetingServiceGrpc;
import io.grpc.*;

import java.io.File;
import java.io.IOException;

public final class GreetingClientTls {
    private static void doGreet(ManagedChannel channel) {
        System.out.println("Enter doGreet");
        GreetingServiceGrpc.GreetingServiceBlockingStub stub = GreetingServiceGrpc.newBlockingStub(channel);
        GreetingResponse response = stub.greet(GreetingRequest.newBuilder().setFirstName("Clement").build());

        System.out.println("Greeting: " + response.getResult());
    }

    public static void main(String[] args) throws IOException {
        ChannelCredentials creds = TlsChannelCredentials.newBuilder().trustManager(
            new File("ssl/ca.crt")
        ).build();
        ManagedChannel channel = Grpc.newChannelBuilderForAddress("localhost", 50051, creds).build();

        doGreet(channel);

        System.out.println("Shutting Down");
        channel.shutdown();
    }
}
