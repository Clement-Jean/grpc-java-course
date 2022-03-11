package utils;

import io.grpc.BindableService;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.AbstractAsyncStub;
import io.grpc.stub.AbstractBlockingStub;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("rawtypes")
public class ServerTestBase<BLOCKING_STUB extends AbstractBlockingStub, ASYNC_STUB extends AbstractAsyncStub> {
    private static final String serverName = "test";

    private Server server;
    private ManagedChannel channel;
    private StubInstantiator<BLOCKING_STUB> blockingStubInstantiator = null;
    private StubInstantiator<ASYNC_STUB> asyncStubInstantiator = null;
    protected BLOCKING_STUB blockingStub = null;
    protected ASYNC_STUB asyncStub = null;

    private final List<BindableService> services = new ArrayList<>();

    protected void addService(BindableService service) {
        this.services.add(service);
    }

    protected void setBlockingStubInstantiator(StubInstantiator<BLOCKING_STUB> stub) {
        this.blockingStubInstantiator = stub;
    }

    protected void setAsyncStubInstantiator(StubInstantiator<ASYNC_STUB> stub) {
        this.asyncStubInstantiator = stub;
    }

    @BeforeEach
    void before() throws Throwable {
        InProcessServerBuilder builder = InProcessServerBuilder.forName(serverName);

        for (BindableService service : services)
            builder.addService(service);

        server = builder.build();
        server.start();

        channel = InProcessChannelBuilder.forName(serverName)
                .directExecutor()
                .usePlaintext()
                .build();

        if (blockingStubInstantiator != null)
            blockingStub = blockingStubInstantiator.instantiate(channel);
        if (asyncStubInstantiator != null)
            asyncStub = asyncStubInstantiator.instantiate(channel);

        if (asyncStub == null && blockingStub == null)
            throw new Throwable("Either blockingStub or asyncStub need to be initialized.");
    }

    @AfterEach
    void after() throws InterruptedException {
        if (!server.isShutdown())
            server.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);

        if (!channel.isShutdown())
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }
}
