package de.martinpallmann.mockbridge.jdk;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.direct.DirectCallHttpServerFactory;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.verification.notmatched.NotMatchedRenderer;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;

public class MockBridgeTest {

    Logger logger = LoggerFactory.getLogger(MockBridgeTest.class);

    @Test
    public void test() throws IOException, InterruptedException {
        final DirectCallHttpServerFactory factory =
                new DirectCallHttpServerFactory();
        final WireMockConfiguration config = wireMockConfig();
        NotMatchedRenderer r = new NotMatchedRenderer() {
            @Override
            protected ResponseDefinition render(Admin admin, Request request) {
                logger.debug("Request: {}", request);
                return null;
            }
        };
        final WireMockServer wm =
                new WireMockServer(config.notMatchedRenderer(r).httpServerFactory(factory));
        final MockBridge client =
                MockBridge
                        .builder()
                        .server(factory.getHttpServer())
                        .build();
        wm.stubFor(get(anyUrl()).willReturn(ok().withBody("OK")));
        final HttpResponse<String> response =
                client.send(
                        HttpRequest.newBuilder(URI.create("http://example.com/")).build(),
                        HttpResponse.BodyHandlers.ofString()
                );
        assertEquals(200, response.statusCode());
        assertEquals("OK", response.body());
    }
}
