package de.martinpallmann.mockbridge.jdk;

import com.github.tomakehurst.wiremock.direct.DirectCallHttpServer;
import com.github.tomakehurst.wiremock.http.Response;
import de.martinpallmann.mockbridge.jdk.api.JdkRequest;
import de.martinpallmann.mockbridge.jdk.api.JdkResponseInfo;
import de.martinpallmann.mockbridge.jdk.api.ResponseSubscription;
import de.martinpallmann.mockbridge.jdk.api.WiremockResponse;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;

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

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class MockBridge extends HttpClient {

    private final DirectCallHttpServer server;
    @Default
    private final Version version = Version.HTTP_1_1;
    @Default
    private final Redirect followRedirects = Redirect.NEVER;
    private final CookieHandler cookieHandler;
    private final ProxySelector proxySelector;
    private final Authenticator authenticator;
    private final Duration connectTimeout;

    @Override
    public Optional<CookieHandler> cookieHandler() {
        return Optional.ofNullable(cookieHandler);
    }

    @Override
    public Optional<Duration> connectTimeout() {
        return Optional.ofNullable(connectTimeout);
    }

    @Override
    public Redirect followRedirects() {
        return followRedirects;
    }

    @Override
    public Optional<ProxySelector> proxy() {
        return Optional.ofNullable(proxySelector);
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

    @Override
    public Version version() {
        return version;
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
            if (cf != null) cf.cancel(true);
            throw ie;
        } catch (ExecutionException e) {
            final Throwable throwable = e.getCause();
            final String msg = throwable.getMessage();

            if (throwable instanceof IllegalArgumentException) {
                throw new IllegalArgumentException(msg, throwable);
            } else if (throwable instanceof SecurityException) {
                throw new SecurityException(msg, throwable);
            } else if (throwable instanceof HttpConnectTimeoutException) {
                HttpConnectTimeoutException hcte = new HttpConnectTimeoutException(msg);
                hcte.initCause(throwable);
                throw hcte;
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
        final Response response = server.stubRequest(new JdkRequest(httpRequest));
        final HttpResponse.ResponseInfo responseInfo = JdkResponseInfo.of(version, response);
        final HttpResponse.BodySubscriber<T> subscriber = bodyHandler.apply(responseInfo);
        subscriber.onSubscribe(new ResponseSubscription(subscriber, response));
        return subscriber
                .getBody()
                .thenApply(WiremockResponse.of(httpRequest, response))
                .toCompletableFuture();
    }
}
