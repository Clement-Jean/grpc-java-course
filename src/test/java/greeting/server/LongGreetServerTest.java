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

public class LongGreetServerTest extends ServerTestBase<GreetServiceGrpc.GreetServiceBlockingStub, GreetServiceGrpc.GreetServiceStub> {

    private String finalResult = "";

    @Nullable
    private Throwable error = null;

    LongGreetServerTest() {
        addService(new GreetingServiceImpl());
        setAsyncStubInstantiator(GreetServiceGrpc::newStub);
    }

    @Test
    void longGreetImplReplyMessage() throws InterruptedException {
        List<String> names = new ArrayList<String>();
        CountDownLatch latch = new CountDownLatch(1);

        Collections.addAll(names, "Clement", "Marie", "Test");

        StreamObserver<GreetRequest> stream = asyncStub.longGreet(new StreamObserver<GreetResponse>() {
            @Override
            public void onNext(GreetResponse response) {
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

        for (String name: names) {
            stream.onNext(GreetRequest.newBuilder().setFirstName(name).build());
        }

        stream.onCompleted();

        boolean reachedZero = latch.await(3, TimeUnit.SECONDS);

        assertTrue(reachedZero);
        assertEquals("Hello Clement!\nHello Marie!\nHello Test!\n", finalResult);
        assertNull(error);
    }
}
