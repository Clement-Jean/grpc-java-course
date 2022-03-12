package greeting.server;

import com.proto.greeting.GreetingRequest;
import com.proto.greeting.GreetingResponse;
import com.proto.greeting.GreetingServiceGrpc;
import org.junit.jupiter.api.Test;
import utils.ServerTestBase;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GreetingServerTest extends ServerTestBase<GreetingServiceGrpc.GreetingServiceBlockingStub, GreetingServiceGrpc.GreetingServiceStub> {

    GreetingServerTest() {
        addService(new GreetingServiceImpl(new SleeperImpl()));
        setBlockingStubInstantiator(GreetingServiceGrpc::newBlockingStub);
    }

    @Test
    void greetImplReplyMessage() {
        GreetingResponse response = blockingStub.greet(GreetingRequest.newBuilder().setFirstName("Clement").build());

        assertEquals("Hello Clement", response.getResult());
    }
}
