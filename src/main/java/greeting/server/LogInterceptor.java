package greeting.server;

import io.grpc.*;

public final class LogInterceptor implements ServerInterceptor {
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(next.startCall(call, headers)) {
            @Override
            public void onMessage(ReqT message) {
                System.out.println("Receive a message");
                System.out.println(message);

                System.out.println("With headers");
                System.out.println(headers);
                super.onMessage(message);
            }
        };
    }
}
