package greeting.client;

import com.proto.greeting.*;
import io.grpc.*;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class GreetingClient {
    private static void doGreet(ManagedChannel channel) {
        System.out.println("Enter doGreet");
        GreetingServiceGrpc.GreetingServiceBlockingStub stub = GreetingServiceGrpc.newBlockingStub(channel);
        GreetingResponse response = stub.greet(GreetingRequest.newBuilder().setFirstName("Clement").build());

        System.out.println("Greeting: " + response.getResult());
    }

    private static void doGreetManyTimes(ManagedChannel channel) {
        System.out.println("Enter doGreetManyTimes");
        GreetingServiceGrpc.GreetingServiceBlockingStub stub = GreetingServiceGrpc.newBlockingStub(channel);

        stub.greetManyTimes(GreetingRequest.newBuilder().setFirstName("Clement").build()).forEachRemaining(response ->
            System.out.println(response.getResult())
        );
    }

    private static void doLongGreet(ManagedChannel channel) throws InterruptedException {
        System.out.println("Enter doLongGreet");
        GreetingServiceGrpc.GreetingServiceStub stub = GreetingServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<GreetingRequest> stream = stub.longGreet(new StreamObserver<GreetingResponse>() {
            @Override
            public void onNext(GreetingResponse response) {
                System.out.println(response.getResult());
            }

            @Override
            public void onError(Throwable t) {}

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });

        Arrays.asList("Clement", "Marie", "Test").forEach(name ->
            stream.onNext(GreetingRequest.newBuilder().setFirstName(name).build())
        );

        stream.onCompleted();

        //noinspection ResultOfMethodCallIgnored
        latch.await(3, TimeUnit.SECONDS);
    }

    private static void doGreetEveryone(ManagedChannel channel) throws InterruptedException {
        System.out.println("Enter doGreetEveryone");
        GreetingServiceGrpc.GreetingServiceStub stub = GreetingServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<GreetingRequest> stream = stub.greetEveryone(new StreamObserver<GreetingResponse>() {
            @Override
            public void onNext(GreetingResponse response) {
                System.out.println(response.getResult());
            }

            @Override
            public void onError(Throwable t) {}

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });

        Arrays.asList("Clement", "Marie", "Test").forEach(name ->
            stream.onNext(GreetingRequest.newBuilder().setFirstName(name).build())
        );

        stream.onCompleted();

        //noinspection ResultOfMethodCallIgnored
        latch.await(3, TimeUnit.SECONDS);
    }

    private static void doGreetWithDeadline(ManagedChannel channel) {
        System.out.println("Enter doGreetWithDeadline");
        GreetingServiceGrpc.GreetingServiceBlockingStub stub = GreetingServiceGrpc.newBlockingStub(channel);
        GreetingRequest request = GreetingRequest.newBuilder().setFirstName("Clement").build();
        GreetingResponse response = stub.withDeadline(Deadline.after(3, TimeUnit.SECONDS))
                .greetWithDeadline(request);

        System.out.println("Greeting within deadline: " + response.getResult());

        try {
            response = stub.withDeadline(Deadline.after(100, TimeUnit.MILLISECONDS))
                    .greetWithDeadline(request);

            System.out.println("Greeting deadline exceeded: " + response.getResult());
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.DEADLINE_EXCEEDED) {
                System.out.println("Deadline has been exceeded");
            } else {
                System.out.println("Got an exception in greetWithDeadline");
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        if (args.length == 0) {
            System.out.println("Need one argument to work");
            return;
        }

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50051)
                .intercept(new LogInterceptor())
                .intercept(new AddHeaderInterceptor())
                .usePlaintext()
                .build();

        switch (args[0]) {
            case "greet": doGreet(channel); break;
            case "greet_many_times": doGreetManyTimes(channel); break;
            case "greet_long": doLongGreet(channel); break;
            case "greet_everyone": doGreetEveryone(channel); break;
            case "greet_with_deadline": doGreetWithDeadline(channel); break;
            default: System.out.println("Keyword Invalid: " + args[0]);
        }

        System.out.println("Shutting Down");
        channel.shutdown();
    }
}
