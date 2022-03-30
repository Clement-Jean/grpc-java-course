package greeting.server;

import com.proto.greeting.GreetingRequest;
import com.proto.greeting.GreetingServiceGrpc;
import io.grpc.*;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.junit.After;
import org.junit.jupiter.api.Test;
import utils.Sleeper;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.fail;

public class GreetingWithDeadlineThrowsTest {

    private static class ThrowingSleeper implements Sleeper {
        @Override
        public void sleep(long millis) throws InterruptedException {
            throw new InterruptedException();
        }
    }

    private Server server;
    private ManagedChannel channel;

    @After
    public void after() throws InterruptedException {
        if (!server.isShutdown())
            server.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);

        if (!channel.isShutdown())
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void greetThrows() throws IOException {
        String serverName = InProcessServerBuilder.generateName();
        server = InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .addService(new GreetingServiceImpl(new ThrowingSleeper()))
                .build()
                .start();
        channel = InProcessChannelBuilder.forName(serverName)
                .directExecutor()
                .build();
        GreetingServiceGrpc.GreetingServiceBlockingStub stub = GreetingServiceGrpc.newBlockingStub(channel);

        try {
            stub.withDeadline(Deadline.after(100, TimeUnit.MILLISECONDS))
                    .greetWithDeadline(GreetingRequest.newBuilder().setFirstName("Clement").build());
            fail("There should be an error in this case");
        } catch (RuntimeException e) {
            // success
            channel.shutdownNow();
            server.shutdownNow();
        }
    }
}
