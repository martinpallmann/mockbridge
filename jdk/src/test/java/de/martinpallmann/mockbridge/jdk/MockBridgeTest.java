package de.martinpallmann.mockbridge.jdk;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static de.martinpallmann.mockbridge.jdk.MockBridge.httpClient;

public class MockBridgeTest {

    final HttpClient client = httpClient();

    @Test
    public void test() throws IOException, InterruptedException {
        stubFor(get(anyUrl()).willReturn(ok().withBody("OK")));
        final HttpResponse<String> response =
                client.send(
                        HttpRequest.newBuilder(URI.create("http://example.com/")).build(),
                        HttpResponse.BodyHandlers.ofString()
                );
        assertEquals(200, response.statusCode());
        assertEquals("OK", response.body());
    }
}
