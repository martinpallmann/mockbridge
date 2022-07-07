package de.martinpallmann.mockbridge.jdk.api;

import lombok.Synchronized;

import java.nio.ByteBuffer;
import java.util.concurrent.Flow;

public class ByteBufferSubscriber implements Flow.Subscriber<ByteBuffer> {

    private ByteBuffer result = ByteBuffer.allocate(0);

    public byte[] result() {
        return result.array();
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        subscription.request(Long.MAX_VALUE);
    }

    @Override
    @Synchronized
    public void onNext(ByteBuffer item) {
        result = ByteBuffer
                    .allocate(result.capacity() + item.capacity())
                    .put(result)
                    .put(item);
    }

    @Override
    public void onError(Throwable throwable) {}

    @Override
    public void onComplete() {}
}
