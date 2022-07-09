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
