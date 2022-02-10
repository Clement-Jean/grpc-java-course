package greeting.server;

import com.proto.greet.GreetRequest;
import com.proto.greet.GreetResponse;
import com.proto.greet.GreetServiceGrpc;
import io.grpc.Deadline;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.Test;
import utils.ServerTestBase;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class GreetWithDeadlineTest extends ServerTestBase<
    GreetServiceGrpc.GreetServiceBlockingStub,
    GreetServiceGrpc.GreetServiceStub
> {
    GreetWithDeadlineTest() {
        addService(new GreetingServiceImpl());
        setBlockingStubInstantiator(GreetServiceGrpc::newBlockingStub);
    }

    @Test
    void greetTest() {
        GreetResponse response = blockingStub.withDeadline(Deadline.after(3, TimeUnit.SECONDS))
                .greetWithDeadline(GreetRequest.newBuilder().setFirstName("Clement").setLastName("Jean").build());

        assertEquals("Hello Clement", response.getResult());
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void greetDeadlineExceededTest() {
        try {
            blockingStub.withDeadline(Deadline.after(100, TimeUnit.MILLISECONDS))
                    .greetWithDeadline(GreetRequest.newBuilder().setFirstName("Clement").setLastName("Jean").build());
            fail("There should be an error in this case");
        } catch (StatusRuntimeException e) {
            Status status = Status.fromThrowable(e);

            assertEquals(Status.Code.DEADLINE_EXCEEDED, status.getCode());
        }
    }
}
