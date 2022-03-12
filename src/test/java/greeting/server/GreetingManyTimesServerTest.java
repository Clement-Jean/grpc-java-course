package greeting.server;

import com.proto.greeting.GreetingRequest;
import com.proto.greeting.GreetingServiceGrpc;
import org.junit.jupiter.api.Test;
import utils.ServerTestBase;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GreetingManyTimesServerTest extends ServerTestBase<GreetingServiceGrpc.GreetingServiceBlockingStub, GreetingServiceGrpc.GreetingServiceStub> {

    GreetingManyTimesServerTest() {
        addService(new GreetingServiceImpl(new SleeperImpl()));
        setBlockingStubInstantiator(GreetingServiceGrpc::newBlockingStub);
    }

    @Test
    void greetManyTimesImplReplyMessage() {
        AtomicInteger count = new AtomicInteger();

        blockingStub.greetManyTimes(GreetingRequest.newBuilder().setFirstName("Clement").build()).forEachRemaining(response -> {
            assertEquals("Hello Clement", response.getResult());
            count.incrementAndGet();
        });

        assertEquals(count.get(), 10);
    }
}
