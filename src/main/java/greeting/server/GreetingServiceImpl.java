package greeting.server;

import com.proto.greet.GreetRequest;
import com.proto.greet.GreetResponse;
import com.proto.greet.GreetServiceGrpc;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;

public class GreetingServiceImpl extends GreetServiceGrpc.GreetServiceImplBase {
    @Override
    public void greet(GreetRequest request, StreamObserver<GreetResponse> responseObserver) {
        responseObserver.onNext(GreetResponse.newBuilder().setResult("Hello " + request.getFirstName()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void greetManyTimes(GreetRequest request, StreamObserver<GreetResponse> responseObserver) {
        for (int i = 0; i < 10; ++i) {
            responseObserver.onNext(GreetResponse.newBuilder().setResult("Hello " + request.getFirstName()).build());
        }

        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<GreetRequest> longGreet(StreamObserver<GreetResponse> responseObserver) {
        StringBuilder sb = new StringBuilder();

        return new StreamObserver<>() {
            @Override
            public void onNext(GreetRequest request) {
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
                responseObserver.onNext(GreetResponse.newBuilder().setResult(sb.toString()).build());
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<GreetRequest> greetEveryone(StreamObserver<GreetResponse> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(GreetRequest request) {
                responseObserver.onNext(GreetResponse.newBuilder().setResult("Hello " + request.getFirstName()).build());
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
    public void greetWithDeadline(GreetRequest request, StreamObserver<GreetResponse> responseObserver) {
        Context current = Context.current();

        try {
            for (int i = 0; i < 3; ++i) {
                if (current.isCancelled())
                    return;

                Thread.sleep(100);
            }

            responseObserver.onNext(GreetResponse.newBuilder().setResult("Hello " + request.getFirstName()).build());
            responseObserver.onCompleted();
        } catch (InterruptedException e) {
            responseObserver.onError(e);
        }
    }
}
