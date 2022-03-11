package calculator.server;

import com.proto.calculator.AvgRequest;
import com.proto.calculator.AvgResponse;
import com.proto.calculator.CalculatorServiceGrpc;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import utils.ServerTestBase;

import javax.annotation.Nullable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class CalculatorAvgTest extends ServerTestBase<
    CalculatorServiceGrpc.CalculatorServiceBlockingStub,
    CalculatorServiceGrpc.CalculatorServiceStub
> {
    private double finalResult;

    @Nullable
    private Throwable error = null;

    CalculatorAvgTest() {
        addService(new CalculatorServiceImpl());
        setAsyncStubInstantiator(CalculatorServiceGrpc::newStub);
    }

    @Test
    void avgTest() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<AvgRequest> stream = asyncStub.avg(new StreamObserver<AvgResponse>() {
            @Override
            public void onNext(AvgResponse response) {
                finalResult = response.getResult();
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

        IntStream.range(1, 11).forEach(number ->
            stream.onNext(AvgRequest.newBuilder().setNumber(number).build())
        );

        stream.onCompleted();

        boolean reachedZero = latch.await(3, TimeUnit.SECONDS);

        assertTrue(reachedZero);
        assertEquals(5.5, finalResult);
        assertNull(error);
    }
}
