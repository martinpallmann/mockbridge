package de.martinpallmann.mockbridge.jdk.api;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.HttpServerFactory;

public class DirectWireMockServer extends WireMockServer {

    public DirectWireMockServer(WireMockConfiguration configuration, HttpServerFactory factory) {
        super(configuration.httpServerFactory(factory));
        WireMock.configureFor(super.client);
    }
}
