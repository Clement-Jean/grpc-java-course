package utils;

import io.grpc.ManagedChannel;
import io.grpc.stub.AbstractStub;

@SuppressWarnings("rawtypes")
public interface StubInstantiator<STUB extends AbstractStub> {
    STUB instantiate(ManagedChannel channel);
}