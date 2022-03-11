package utils;

import io.grpc.BindableService;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.AbstractStub;
import org.junit.jupiter.api.AfterEach;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("rawtypes")
    public class ClientTestBase<STUB extends AbstractStub> {

    private Server server;
    private ManagedChannel channel;
    private STUB stub;

    @AfterEach
    void after() throws InterruptedException {
        if (!server.isShutdown())
            server.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);

        if (!channel.isShutdown())
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }

    protected STUB getStub() { return stub; }

    protected void createServerWithService(
        BindableService service,
        StubInstantiator<STUB> instantiator
    ) throws IOException {
        String serverName = InProcessServerBuilder.generateName();
        server = InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .addService(service)
                .build()
                .start();
        channel = InProcessChannelBuilder.forName(serverName)
                .directExecutor()
                .build();
        stub = instantiator.instantiate(channel);
    }
}
