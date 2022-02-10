package greeting.client;

import com.proto.greet.*;
import io.grpc.Deadline;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GreetingClient {
    private static void doGreet(ManagedChannel channel) {
        System.out.println("Enter doGreet");
        GreetServiceGrpc.GreetServiceBlockingStub stub = GreetServiceGrpc.newBlockingStub(channel);
        GreetResponse response = stub.greet(GreetRequest.newBuilder().setFirstName("Clement").setLastName("Jean").build());

        System.out.println("Greeting: " + response.getResult());
    }

    private static void doGreetManyTimes(ManagedChannel channel) {
        System.out.println("Enter doGreetManyTimes");
        GreetServiceGrpc.GreetServiceBlockingStub stub = GreetServiceGrpc.newBlockingStub(channel);

        stub.greetManyTimes(GreetRequest.newBuilder().setFirstName("Clement").build()).forEachRemaining(response -> {
            System.out.println(response.getResult());
        });
    }

    private static void doLongGreet(ManagedChannel channel) throws InterruptedException {
        System.out.println("Enter doLongGreet");
        GreetServiceGrpc.GreetServiceStub stub = GreetServiceGrpc.newStub(channel);

        List<String> names = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        Collections.addAll(names, "Clement", "Marie", "Test");

        StreamObserver<GreetRequest> stream = stub.longGreet(new StreamObserver<>() {
            @Override
            public void onNext(GreetResponse response) {
                System.out.println(response.getResult());
            }

            @Override
            public void onError(Throwable t) {}

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });

        for (String name: names) {
            stream.onNext(GreetRequest.newBuilder().setFirstName(name).build());
        }

        stream.onCompleted();

        latch.await(3, TimeUnit.SECONDS);
    }

    private static void doGreetEveryone(ManagedChannel channel) throws InterruptedException {
        System.out.println("Enter doGreetEveryone");
        GreetServiceGrpc.GreetServiceStub stub = GreetServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<GreetRequest> stream = stub.greetEveryone(new StreamObserver<>() {
            @Override
            public void onNext(GreetResponse response) {
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
            stream.onNext(GreetRequest.newBuilder().setFirstName(name).build())
        );

        stream.onCompleted();

        latch.await(3, TimeUnit.SECONDS);
    }

    private static void doGreetWithDeadline(ManagedChannel channel) {
        System.out.println("Enter doGreetWithDeadline");
        GreetServiceGrpc.GreetServiceBlockingStub stub = GreetServiceGrpc.newBlockingStub(channel);
        GreetResponse response = stub.withDeadline(Deadline.after(3, TimeUnit.SECONDS))
                .greetWithDeadline(GreetRequest.newBuilder().setFirstName("Clement").setLastName("Jean").build());

        System.out.println("Greeting within deadline: " + response.getResult());

        response = stub.withDeadline(Deadline.after(100, TimeUnit.MILLISECONDS))
                .greetWithDeadline(GreetRequest.newBuilder().setFirstName("Clement").setLastName("Jean").build());

        System.out.println("Greeting deadline exceeded: " + response.getResult());
    }

    public static void main(String[] args) throws InterruptedException {
        if (args.length == 0) {
            System.out.println("Need one argument to work");
        }

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051).usePlaintext().build();

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
