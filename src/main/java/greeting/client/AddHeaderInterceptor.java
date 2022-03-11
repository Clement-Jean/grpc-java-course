package greeting.client;

import io.grpc.*;

import static greeting.server.HeaderCheckInterceptor.CUSTOM_HEADER_KEY;

public final class AddHeaderInterceptor implements ClientInterceptor {
    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                headers.put(CUSTOM_HEADER_KEY, "customRequestValue");
                super.start(responseListener, headers);
            }
        };
    }
}
