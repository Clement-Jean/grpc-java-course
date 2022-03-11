package greeting.server;

import com.google.common.annotations.VisibleForTesting;
import io.grpc.*;

public final class HeaderCheckInterceptor implements ServerInterceptor {

    @VisibleForTesting
    public static final Metadata.Key<String> CUSTOM_HEADER_KEY =
            Metadata.Key.of("custom_server_header_key", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        if (!headers.containsKey(CUSTOM_HEADER_KEY)) {
            call.close(Status.CANCELLED, new Metadata());
            return new ServerCall.Listener<ReqT>() {};
        }

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(next.startCall(call, headers)) {};
    }
}
