package calculator.server;

import com.proto.calculator.CalculatorServiceGrpc;
import com.proto.calculator.PrimeRequest;
import org.junit.jupiter.api.Test;
import utils.ServerTestBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CalculatorPrimesTest extends ServerTestBase<
    CalculatorServiceGrpc.CalculatorServiceBlockingStub,
    CalculatorServiceGrpc.CalculatorServiceStub
> {
    CalculatorPrimesTest() {
        addService(new CalculatorServiceImpl());
        setBlockingStubInstantiator(CalculatorServiceGrpc::newBlockingStub);
    }

    private final List<Integer> results = new ArrayList<>();

    @Test
    void primesTest() {
        blockingStub.primes(PrimeRequest.newBuilder().setNumber(567890).build()).forEachRemaining(prime ->
            results.add(prime.getPrimeFactor())
        );

        List<Integer> primes = new ArrayList<>();
        Collections.addAll(primes, 2, 5, 109, 521);

        assertEquals(results, primes);
    }
}
