package de.martinpallmann.mockbridge.jdk.api;

import com.github.tomakehurst.wiremock.http.Response;
import lombok.RequiredArgsConstructor;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.Flow;

@RequiredArgsConstructor
public class ResponseSubscription implements Flow.Subscription {

    private final Flow.Subscriber<List<ByteBuffer>> subscriber;
    private final Response response;

    @Override
    public void request(long n) {
        if (n > 0) {
            if (response.getBody() != null) {
                subscriber.onNext(List.of(ByteBuffer.wrap(response.getBody())));
            }
            subscriber.onComplete();
        } else {
            subscriber.onError(new IllegalArgumentException("n must be > 0"));
        }
    }

    @Override
    public void cancel() {
        subscriber.onComplete();
    }
}
