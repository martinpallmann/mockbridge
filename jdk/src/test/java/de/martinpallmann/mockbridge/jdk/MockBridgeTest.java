package de.martinpallmann.mockbridge.jdk;

import com.github.tomakehurst.wiremock.http.Body;
import com.github.tomakehurst.wiremock.http.ContentTypeHeader;
import de.martinpallmann.mockbridge.tck.TckSuite;
import de.martinpallmann.mockbridge.tck.TestRequest;
import de.martinpallmann.mockbridge.tck.TestResponse;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.rmi.server.ExportException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static de.martinpallmann.mockbridge.jdk.MockBridge.httpClient;

class MockBridgeTest extends TckSuite {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    final HttpClient client = httpClient();

    @Override
    protected TestResponse send(TestRequest request) {
        try {
            final HttpResponse<byte[]> response =
                    client.send(
                            HttpRequest.newBuilder(URI.create(request.getUrl()))
                                .method(request.getMethod(), HttpRequest.BodyPublishers.noBody())
                                .build(),
                            HttpResponse.BodyHandlers.ofByteArray()
                    );
            return TestResponse
                    .builder()
                    .status(response.statusCode())
                    .body(Body.ofBinaryOrText(
                            response.body(),
                            response.headers()
                                    .firstValue("Content-Type")
                                    .map(ContentTypeHeader::new)
                                    .orElse(ContentTypeHeader.absent())
                            ))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
