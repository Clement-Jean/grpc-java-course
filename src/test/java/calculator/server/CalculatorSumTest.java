package calculator.server;

import com.proto.calculator.CalculatorServiceGrpc;
import com.proto.calculator.SumRequest;
import com.proto.calculator.SumResponse;
import org.junit.jupiter.api.Test;
import utils.ServerTestBase;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CalculatorSumTest extends ServerTestBase<
    CalculatorServiceGrpc.CalculatorServiceBlockingStub,
    CalculatorServiceGrpc.CalculatorServiceStub
> {

    CalculatorSumTest() {
        addService(new CalculatorServiceImpl());
        setBlockingStubInstantiator(CalculatorServiceGrpc::newBlockingStub);
    }

    @Test
    void sumTest() {
        SumResponse response = blockingStub.sum(SumRequest.newBuilder().setFirstNumber(1).setSecondNumber(1).build());

        assertEquals(2, response.getResult());
    }
}
