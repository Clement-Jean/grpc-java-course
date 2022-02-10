package greeting.server;

import com.proto.greet.GreetRequest;
import com.proto.greet.GreetServiceGrpc;
import org.junit.jupiter.api.Test;
import utils.ServerTestBase;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GreetManyTimesServerTest extends ServerTestBase<GreetServiceGrpc.GreetServiceBlockingStub, GreetServiceGrpc.GreetServiceStub> {

    GreetManyTimesServerTest() {
        addService(new GreetingServiceImpl());
        setBlockingStubInstantiator(GreetServiceGrpc::newBlockingStub);
    }

    @Test
    void greetManyTimesImplReplyMessage() {
        AtomicInteger count = new AtomicInteger();

        blockingStub.greetManyTimes(GreetRequest.newBuilder().setFirstName("Clement").build()).forEachRemaining(response -> {
            assertEquals("Hello Clement", response.getResult());
            count.incrementAndGet();
        });

        assertEquals(count.get(), 10);
    }
}
