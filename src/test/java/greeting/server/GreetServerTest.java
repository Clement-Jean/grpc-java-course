package greeting.server;

import com.proto.greet.GreetRequest;
import com.proto.greet.GreetResponse;
import com.proto.greet.GreetServiceGrpc;
import org.junit.jupiter.api.Test;
import utils.ServerTestBase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class GreetServerTest extends ServerTestBase<GreetServiceGrpc.GreetServiceBlockingStub, GreetServiceGrpc.GreetServiceStub> {

    GreetServerTest() {
        addService(new GreetingServiceImpl());
        setBlockingStubInstantiator(GreetServiceGrpc::newBlockingStub);
    }

    @Test
    void greetImplReplyMessage() {
        GreetResponse response = blockingStub.greet(GreetRequest.newBuilder().setFirstName("Clement").build());

        assertEquals("Hello Clement", response.getResult());
    }
}
