package greeting.server;

import com.proto.greeting.GreetingRequest;
import com.proto.greeting.GreetingResponse;
import com.proto.greeting.GreetingServiceGrpc;
import io.grpc.Deadline;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.Test;
import utils.ServerTestBase;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class GreetingWithDeadlineTest extends ServerTestBase<
    GreetingServiceGrpc.GreetingServiceBlockingStub,
    GreetingServiceGrpc.GreetingServiceStub
> {
    GreetingWithDeadlineTest() {
        addService(new GreetingServiceImpl(new SleeperImpl()));
        setBlockingStubInstantiator(GreetingServiceGrpc::newBlockingStub);
    }

    @Test
    void greetTest() {
        GreetingResponse response = blockingStub.withDeadline(Deadline.after(3, TimeUnit.SECONDS))
                .greetWithDeadline(GreetingRequest.newBuilder().setFirstName("Clement").build());

        assertEquals("Hello Clement", response.getResult());
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void greetDeadlineExceededTest() {
        try {
            blockingStub.withDeadline(Deadline.after(100, TimeUnit.MILLISECONDS))
                    .greetWithDeadline(GreetingRequest.newBuilder().setFirstName("Clement").build());
            fail("There should be an error in this case");
        } catch (StatusRuntimeException e) {
            Status status = Status.fromThrowable(e);

            assertEquals(Status.Code.DEADLINE_EXCEEDED, status.getCode());
        }
    }
}
