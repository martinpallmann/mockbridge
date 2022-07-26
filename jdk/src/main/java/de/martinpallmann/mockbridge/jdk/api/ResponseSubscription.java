/*
 *    Copyright (c) 2022 Martin Pallmann
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

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
