package greeting.server;

import com.proto.greeting.GreetingRequest;
import com.proto.greeting.GreetingResponse;
import com.proto.greeting.GreetingServiceGrpc;
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

public class LongGreetingServerTest extends ServerTestBase<GreetingServiceGrpc.GreetingServiceBlockingStub, GreetingServiceGrpc.GreetingServiceStub> {

    private String finalResult = "";

    @Nullable
    private Throwable error = null;

    LongGreetingServerTest() {
        addService(new GreetingServiceImpl(new SleeperImpl()));
        setAsyncStubInstantiator(GreetingServiceGrpc::newStub);
    }

    @Test
    void longGreetImplReplyMessage() throws InterruptedException {
        List<String> names = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        Collections.addAll(names, "Clement", "Marie", "Test");

        StreamObserver<GreetingRequest> stream = asyncStub.longGreet(new StreamObserver<GreetingResponse>() {
            @Override
            public void onNext(GreetingResponse response) {
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
            stream.onNext(GreetingRequest.newBuilder().setFirstName(name).build());
        }

        stream.onCompleted();

        boolean reachedZero = latch.await(3, TimeUnit.SECONDS);

        assertTrue(reachedZero);
        assertEquals("Hello Clement!\nHello Marie!\nHello Test!\n", finalResult);
        assertNull(error);
    }
}
