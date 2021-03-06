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

package de.martinpallmann.mockbridge.jdk;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.direct.DirectCallHttpServer;
import com.github.tomakehurst.wiremock.direct.DirectCallHttpServerFactory;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.github.tomakehurst.wiremock.verification.NearMiss;
import de.martinpallmann.mockbridge.jdk.api.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLParameters;
import java.io.IOException;
import java.net.Authenticator;
import java.net.ConnectException;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.http.*;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static de.martinpallmann.mockbridge.jdk.api.JdkRequest.request;
import static de.martinpallmann.mockbridge.jdk.api.JdkResponseInfo.responseInfo;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MockBridge extends HttpClient {

    private static final Logger logger = LoggerFactory.getLogger(MockBridge.class);

    private final DirectCallHttpServer server;
    private final Version version;
    private final Redirect followRedirects;
    private final CookieHandler cookieHandler;
    private final ProxySelector proxySelector;
    private final Authenticator authenticator;
    private final Duration connectTimeout;

    public static MockBridge httpClient(WireMockConfiguration configuration) {
        final DirectCallHttpServerFactory factory = new DirectCallHttpServerFactory();
        new DirectWireMockServer(configuration, factory);
        return new MockBridge(
                factory.getHttpServer(),
                Version.HTTP_1_1,
                Redirect.NEVER,
                null,
                null,
                null,
                null
        );
    }

    public static MockBridge httpClient() {
        return httpClient(wireMockConfig());
    }

    @Override
    public Optional<CookieHandler> cookieHandler() {
        return Optional.ofNullable(cookieHandler);
    }

    /**
     * add a CookieHandler to the HttpClient
     *
     * @param cookieHandler the new cookie handler
     * @return the new http client with the desired cookie handler
     */
    public MockBridge cookieHandler(CookieHandler cookieHandler) {
        return new MockBridge(
                server,
                version,
                followRedirects,
                cookieHandler,
                proxySelector,
                authenticator,
                connectTimeout
        );
    }

    @Override
    public Optional<Duration> connectTimeout() {
        return Optional.ofNullable(connectTimeout);
    }

    public MockBridge connectTimeout(Duration connectTimeout) {
        return new MockBridge(
                server,
                version,
                followRedirects,
                cookieHandler,
                proxySelector,
                authenticator,
                connectTimeout
        );
    }

    @Override
    public Redirect followRedirects() {
        return followRedirects;
    }

    public MockBridge followRedirects(Redirect followRedirects) {
        return new MockBridge(
                server,
                version,
                followRedirects,
                cookieHandler,
                proxySelector,
                authenticator,
                connectTimeout
        );
    }

    @Override
    public Optional<ProxySelector> proxy() {
        return Optional.ofNullable(proxySelector);
    }

    public MockBridge proxy(ProxySelector proxySelector) {
        return new MockBridge(
                server,
                version,
                followRedirects,
                cookieHandler,
                proxySelector,
                authenticator,
                connectTimeout
        );
    }

    @Override
    public SSLContext sslContext() {
        return null;
    }

    @Override
    public SSLParameters sslParameters() {
        return null;
    }

    @Override
    public Optional<Authenticator> authenticator() {
        return Optional.ofNullable(authenticator);
    }

    public MockBridge authenticator(Authenticator authenticator) {
        return new MockBridge(
                server,
                version,
                followRedirects,
                cookieHandler,
                proxySelector,
                authenticator,
                connectTimeout
        );
    }

    @Override
    public Version version() {
        return version;
    }

    public MockBridge version(Version version) {
        return new MockBridge(
                server,
                version,
                followRedirects,
                cookieHandler,
                proxySelector,
                authenticator,
                connectTimeout
        );
    }

    @Override
    public Optional<Executor> executor() {
        return Optional.empty();
    }

    @Override
    public <T> HttpResponse<T> send(
            HttpRequest request,
            HttpResponse.BodyHandler<T> responseBodyHandler
    ) throws IOException, InterruptedException {
        CompletableFuture<HttpResponse<T>> cf = null;
        try {
            cf = sendAsync(request, responseBodyHandler, null);
            return cf.get();
        } catch (InterruptedException ie) {
            cf.cancel(true);
            throw ie;
        } catch (ExecutionException e) {
            final Throwable throwable = e.getCause();
            final String msg = throwable.getMessage();

            if (throwable instanceof IllegalArgumentException) {
                throw new IllegalArgumentException(msg, throwable);
            } else if (throwable instanceof SecurityException) {
                throw new SecurityException(msg, throwable);
            } else if (throwable instanceof HttpConnectTimeoutException) {
                HttpConnectTimeoutException exception = new HttpConnectTimeoutException(msg);
                exception.initCause(throwable);
                throw exception;
            } else if (throwable instanceof HttpTimeoutException) {
                throw new HttpTimeoutException(msg);
            } else if (throwable instanceof ConnectException) {
                ConnectException ce = new ConnectException(msg);
                ce.initCause(throwable);
                throw ce;
            } else if (throwable instanceof SSLHandshakeException) {
                // special case for SSLHandshakeException
                SSLHandshakeException he = new SSLHandshakeException(msg);
                he.initCause(throwable);
                throw he;
            } else if (throwable instanceof SSLException) {
                // any other SSLException is wrapped in a plain
                // SSLException
                throw new SSLException(msg, throwable);
            } else if (throwable instanceof IOException) {
                throw new IOException(msg, throwable);
            } else {
                throw new IOException(msg, throwable);
            }
        }
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(
            HttpRequest request,
            HttpResponse.BodyHandler<T> responseHandler
    ) {
        return sendAsync(request, responseHandler, null);
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(
            HttpRequest httpRequest,
            HttpResponse.BodyHandler<T> bodyHandler,
            HttpResponse.PushPromiseHandler<T> pushPromiseHandler
    ) {
        final Response response = server.stubRequest(request(httpRequest));
        for (LoggedRequest r: WireMock.findUnmatchedRequests()) {
            logger.warn("unmatched request: {}", r);
            for (NearMiss m: WireMock.findNearMissesFor(r)) {
                logger.warn("near miss: {}", m);
            }
        }
        final HttpResponse.ResponseInfo responseInfo = responseInfo(version, response);
        final HttpResponse.BodySubscriber<T> subscriber = bodyHandler.apply(responseInfo);
        subscriber.onSubscribe(new ResponseSubscription(subscriber, response));
        return subscriber
                .getBody()
                .thenApply(WiremockResponse.of(httpRequest, response))
                .toCompletableFuture();
    }
}
