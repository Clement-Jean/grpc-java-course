package calculator.server;

import com.proto.calculator.CalculatorServiceGrpc;
import com.proto.calculator.SqrtRequest;
import com.proto.calculator.SqrtResponse;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.Test;
import utils.ServerTestBase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class CalculatorSqrtTest extends ServerTestBase<
    CalculatorServiceGrpc.CalculatorServiceBlockingStub,
    CalculatorServiceGrpc.CalculatorServiceStub
> {

    CalculatorSqrtTest() {
        addService(new CalculatorServiceImpl());
        setBlockingStubInstantiator(CalculatorServiceGrpc::newBlockingStub);
    }

    @Test
    void sqrtTest() {
        SqrtResponse response = blockingStub.sqrt(SqrtRequest.newBuilder().setNumber(25).build());

        assertEquals(5, response.getResult());
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void sqrtErrorTest() {
        try {
            blockingStub.sqrt(SqrtRequest.newBuilder().setNumber(-1).build());
            fail("There should be an error in this case");
        } catch (StatusRuntimeException e) {
            Status status = Status.fromThrowable(e);

            assertEquals(Status.Code.INVALID_ARGUMENT, status.getCode());
            assertEquals("The number being sent cannot be negative\nNumber: -1", status.getDescription());
        }
    }
}
