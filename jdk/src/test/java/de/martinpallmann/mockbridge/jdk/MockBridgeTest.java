package de.martinpallmann.mockbridge.jdk;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ContentTypes;
import com.github.tomakehurst.wiremock.http.Body;
import com.github.tomakehurst.wiremock.http.ContentTypeHeader;
import de.martinpallmann.mockbridge.tck.TckSuite;
import de.martinpallmann.mockbridge.tck.TestRequest;
import de.martinpallmann.mockbridge.tck.TestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static de.martinpallmann.mockbridge.jdk.MockBridge.httpClient;

class MockBridgeTest extends TckSuite {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    final HttpClient client = httpClient();

    @Override
    protected TestResponse send(TestRequest request) throws IOException, InterruptedException {

        final HttpResponse<byte[]> response =
                client.send(
                        HttpRequest.newBuilder(URI.create(request.getAbsoluteUrl()))
                            .method(request.getMethod(), HttpRequest.BodyPublishers.noBody())
                            .build(),
                        HttpResponse.BodyHandlers.ofByteArray()
                );

        final boolean isText = ContentTypes.determineIsTextFromMimeType(response
                .headers()
                .firstValue("Content-Type")
                .map(ContentTypeHeader::new)
                .orElse(ContentTypeHeader.absent())
                .mimeTypePart());

        final Body body;
        if (response.body().length == 0) {
            body = Body.none();
        } else if (isText) {
            body = new Body(new String(response.body()));
        } else {
            body = Body.none();
        }

        return TestResponse
                .builder()
                .status(response.statusCode())
                .body(body)
                .build();
    }
}
