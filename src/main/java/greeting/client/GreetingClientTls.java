package greeting.client;

import com.proto.greet.GreetRequest;
import com.proto.greet.GreetResponse;
import com.proto.greet.GreetServiceGrpc;
import io.grpc.*;

import java.io.File;
import java.io.IOException;

public class GreetingClientTls {
    private static void doGreet(ManagedChannel channel) {
        System.out.println("Enter doGreet");
        GreetServiceGrpc.GreetServiceBlockingStub stub = GreetServiceGrpc.newBlockingStub(channel);
        GreetResponse response = stub.greet(GreetRequest.newBuilder().setFirstName("Clement").setLastName("Jean").build());

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
