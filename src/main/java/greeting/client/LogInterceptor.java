package greeting.client;

import io.grpc.*;

public final class LogInterceptor implements ClientInterceptor {
    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
            @Override
            public void sendMessage(ReqT message) {
                System.out.println("Send a message");
                System.out.println(message);

                System.out.println("With call options");
                System.out.println(callOptions.toString());
                super.sendMessage(message);
            }
        };
    }
}
