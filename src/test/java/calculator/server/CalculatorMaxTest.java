package calculator.server;

import com.proto.calculator.CalculatorServiceGrpc;
import com.proto.calculator.MaxRequest;
import com.proto.calculator.MaxResponse;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import utils.ServerTestBase;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class CalculatorMaxTest extends ServerTestBase<
    CalculatorServiceGrpc.CalculatorServiceBlockingStub,
    CalculatorServiceGrpc.CalculatorServiceStub
> {
    private final List<Integer> finalResult = new ArrayList<>();

    @Nullable
    private Throwable error = null;

    CalculatorMaxTest() {
        addService(new CalculatorServiceImpl());
        setAsyncStubInstantiator(CalculatorServiceGrpc::newStub);
    }

    @Test
    void maxTest() throws InterruptedException {
        List<Integer> numbers = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        Collections.addAll(numbers, 1, 5, 3, 6, 2, 20);

        StreamObserver<MaxRequest> stream = asyncStub.max(new StreamObserver<MaxResponse>() {
            @Override
            public void onNext(MaxResponse response) {
                finalResult.add(response.getMax());
            }

            @Override
            public void onError(Throwable t) {
                error = t;
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });

        for (int number : numbers) {
            stream.onNext(MaxRequest.newBuilder().setNumber(number).build());
        }

        stream.onCompleted();

        boolean reachedZero = latch.await(3, TimeUnit.SECONDS);
        List<Integer> expected = new ArrayList<>();

        Collections.addAll(expected, 1, 5, 6, 20);

        assertTrue(reachedZero);
        assertEquals(finalResult, expected);
        assertNull(error);
    }
}
