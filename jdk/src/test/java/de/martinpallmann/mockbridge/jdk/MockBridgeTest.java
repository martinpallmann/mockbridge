package de.martinpallmann.mockbridge.jdk;

import com.github.tomakehurst.wiremock.common.ContentTypes;
import com.github.tomakehurst.wiremock.http.Body;
import com.github.tomakehurst.wiremock.http.ContentTypeHeader;
import de.martinpallmann.mockbridge.tck.TckSuite;
import de.martinpallmann.mockbridge.tck.TestRequest;
import de.martinpallmann.mockbridge.tck.TestResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieManager;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static de.martinpallmann.mockbridge.jdk.MockBridge.httpClient;
import static java.net.http.HttpClient.Redirect.ALWAYS;
import static java.net.http.HttpClient.Redirect.NEVER;
import static java.net.http.HttpClient.Version.HTTP_1_1;
import static java.net.http.HttpClient.Version.HTTP_2;
import static org.junit.jupiter.api.Assertions.*;

class MockBridgeTest extends TckSuite {

    final MockBridge client = httpClient();

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

    @Test
    public void cookieHandler() {
        assertFalse(client.cookieHandler().isPresent());
        final MockBridge newClient = client.cookieHandler(new CookieManager());
        assertTrue(newClient.cookieHandler().isPresent());
    }

    @Test
    public void connectTimeout() {
        assertFalse(client.connectTimeout().isPresent());
        final MockBridge newClient = client.connectTimeout(Duration.of(10, ChronoUnit.MINUTES));
        assertTrue(newClient.connectTimeout().isPresent());
    }

    @Test
    public void followRedirects() {
        assertEquals(NEVER, client.followRedirects());
        final MockBridge newClient = client.followRedirects(ALWAYS);
        assertEquals(ALWAYS, newClient.followRedirects());
    }

    @Test
    public void proxy() {
        assertFalse(client.proxy().isPresent());
        final MockBridge newClient = client.proxy(ProxySelector.getDefault());
        assertTrue(newClient.proxy().isPresent());
    }

    @Test
    public void authenticator() {
        assertFalse(client.authenticator().isPresent());
        final MockBridge newClient = client.authenticator(new Authenticator(){});
        assertTrue(newClient.authenticator().isPresent());
    }

    @Test
    public void version() {
        assertEquals(HTTP_1_1, client.version());
        final MockBridge newClient = client.version(HTTP_2);
        assertEquals(HTTP_2, newClient.version());
    }
}
