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
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import javax.net.ssl.SSLSession;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.function.Function;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class WiremockResponse<T> implements HttpResponse<T> {

    private final T body;
    private final HttpRequest request;
    private final Response response;
    private final Converter converter = Converter.getInstance();

    public static <T> Function<T, HttpResponse<T>> of(HttpRequest request, Response response) {
        return body -> new WiremockResponse<>(body, request, response);
    }

    @Override
    public int statusCode() {
        return response.getStatus();
    }

    @Override
    public HttpRequest request() {
        return request;
    }

    @Override
    public Optional<HttpResponse<T>> previousResponse() {
        return Optional.empty();
    }

    @Override
    public HttpHeaders headers() {
        return converter.fromWiremock(response.getHeaders());
    }

    @Override
    public T body() {
        return body;
    }

    @Override
    public Optional<SSLSession> sslSession() {
        return Optional.empty();
    }

    @Override
    public URI uri() {
        return request.uri();
    }

    @Override
    public HttpClient.Version version() {
        return request.version().orElse(HttpClient.Version.HTTP_1_1);
    }
}
