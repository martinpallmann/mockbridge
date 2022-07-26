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
