package greeting.server;

import com.proto.greet.GreetRequest;
import com.proto.greet.GreetResponse;
import com.proto.greet.GreetServiceGrpc;
import greeting.client.AddHeaderInterceptor;
import io.grpc.*;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class InterceptorTest {

    @Test
    void interceptPassingTest() throws IOException, InterruptedException {
        String serverName = "test";
        InProcessServerBuilder builder = InProcessServerBuilder.forName(serverName)
                .addService(new GreetingServiceImpl())
                .intercept(new HeaderCheckInterceptor());

        Server server = builder.build();
        server.start();

        ManagedChannel channel = InProcessChannelBuilder.forName(serverName)
                .directExecutor()
                .usePlaintext()
                .intercept(new AddHeaderInterceptor())
                .build();

        GreetServiceGrpc.GreetServiceBlockingStub blockingStub = GreetServiceGrpc.newBlockingStub(channel);

        GreetResponse response = blockingStub.greet(GreetRequest.newBuilder().setFirstName("Clement").build());

        assertEquals("Hello Clement", response.getResult());
        if (!server.isShutdown())
            server.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);

        if (!channel.isShutdown())
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void interceptErrorTest() throws IOException, InterruptedException {
        String serverName = "test";
        InProcessServerBuilder builder = InProcessServerBuilder.forName(serverName)
                .addService(new GreetingServiceImpl())
                .intercept(new HeaderCheckInterceptor());

        Server server = builder.build();
        server.start();

        ManagedChannel channel = InProcessChannelBuilder.forName(serverName)
                .directExecutor()
                .usePlaintext()
                .build();

        GreetServiceGrpc.GreetServiceBlockingStub blockingStub = GreetServiceGrpc.newBlockingStub(channel);

        try {
            blockingStub.greet(GreetRequest.newBuilder().setFirstName("Clement").build());
            fail("There should be an error in this case");
        } catch (StatusRuntimeException e) {
            Status status = Status.fromThrowable(e);

            assertEquals(Status.Code.CANCELLED, status.getCode());
        }

        if (!server.isShutdown())
            server.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);

        if (!channel.isShutdown())
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }
}
