package de.martinpallmann.mockbridge.jdk.api;

import com.github.tomakehurst.wiremock.http.Response;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class JdkResponseInfo implements HttpResponse.ResponseInfo {

    private final HttpClient.Version version;
    private final Response response;
    private final Converter converter;

    public static HttpResponse.ResponseInfo responseInfo(HttpClient.Version version, Response response) {
        return new JdkResponseInfo(version, response, Converter.getInstance());
    }

    @Override
    public int statusCode() {
        return response.getStatus();
    }

    @Override
    public HttpHeaders headers() {
        return converter.fromWiremock(response.getHeaders());
    }

    @Override
    public HttpClient.Version version() {
        return version;
    }
}
