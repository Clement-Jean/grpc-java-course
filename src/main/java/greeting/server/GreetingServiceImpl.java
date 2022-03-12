package greeting.server;

import com.proto.greeting.GreetingRequest;
import com.proto.greeting.GreetingResponse;
import com.proto.greeting.GreetingServiceGrpc;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;
import utils.Sleeper;

public final class GreetingServiceImpl extends GreetingServiceGrpc.GreetingServiceImplBase {

    final Sleeper sleeper;

    GreetingServiceImpl(Sleeper sleeper) {
        this.sleeper = sleeper;
    }

    @Override
    public void greet(GreetingRequest request, StreamObserver<GreetingResponse> responseObserver) {
        responseObserver.onNext(GreetingResponse.newBuilder().setResult("Hello " + request.getFirstName()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void greetManyTimes(GreetingRequest request, StreamObserver<GreetingResponse> responseObserver) {
        GreetingResponse response = GreetingResponse.newBuilder().setResult("Hello " + request.getFirstName()).build();

        for (int i = 0; i < 10; ++i) {
            responseObserver.onNext(response);
        }

        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<GreetingRequest> longGreet(StreamObserver<GreetingResponse> responseObserver) {
        StringBuilder sb = new StringBuilder();

        return new StreamObserver<GreetingRequest>() {
            @Override
            public void onNext(GreetingRequest request) {
                sb.append("Hello ")
                  .append(request.getFirstName())
                  .append("!\n");
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(GreetingResponse.newBuilder().setResult(sb.toString()).build());
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<GreetingRequest> greetEveryone(StreamObserver<GreetingResponse> responseObserver) {
        return new StreamObserver<GreetingRequest>() {
            @Override
            public void onNext(GreetingRequest request) {
                responseObserver.onNext(GreetingResponse.newBuilder().setResult("Hello " + request.getFirstName()).build());
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void greetWithDeadline(GreetingRequest request, StreamObserver<GreetingResponse> responseObserver) {
        Context current = Context.current();

        try {
            for (int i = 0; i < 3; ++i) {
                if (current.isCancelled())
                    return;

                sleeper.sleep(100);
            }

            responseObserver.onNext(GreetingResponse.newBuilder().setResult("Hello " + request.getFirstName()).build());
            responseObserver.onCompleted();
        } catch (InterruptedException e) {
            responseObserver.onError(e);
        }
    }
}
