package greeting.server;

import com.proto.greet.GreetRequest;
import com.proto.greet.GreetResponse;
import com.proto.greet.GreetServiceGrpc;
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

public class GreetEveryoneServerTest extends ServerTestBase<GreetServiceGrpc.GreetServiceBlockingStub, GreetServiceGrpc.GreetServiceStub> {

    private final List<String> finalResult = new ArrayList<String>();

    @Nullable
    private Throwable error = null;

    GreetEveryoneServerTest() {
        addService(new GreetingServiceImpl());
        setAsyncStubInstantiator(GreetServiceGrpc::newStub);
    }

    @Test
    void greetEveryoneImplReplyMessage() throws InterruptedException {
        List<String> names = new ArrayList<String>();
        CountDownLatch latch = new CountDownLatch(1);

        Collections.addAll(names, "Clement", "Marie", "Test");

        StreamObserver<GreetRequest> stream = asyncStub.greetEveryone(new StreamObserver<GreetResponse>() {
            @Override
            public void onNext(GreetResponse response) {
                finalResult.add(response.getResult());
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

        for (String name: names) {
            stream.onNext(GreetRequest.newBuilder().setFirstName(name).build());
        }

        stream.onCompleted();

        boolean reachedZero = latch.await(3, TimeUnit.SECONDS);

        assertTrue(reachedZero);
        assertEquals(finalResult, names.stream().map(name -> "Hello " + name).toList());
        assertNull(error);
    }
}
