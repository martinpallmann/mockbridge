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

package de.martinpallmann.mockbridge.jdk.api;

import com.github.tomakehurst.wiremock.common.Encoding;
import com.github.tomakehurst.wiremock.common.Urls;
import com.github.tomakehurst.wiremock.http.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpRequest;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class JdkRequest implements Request {

    private final Logger logger = LoggerFactory.getLogger(JdkRequest.class);

    private final HttpRequest wrapped;
    private final Converter converter;

    public static Request request(HttpRequest wrapped) {
        return new JdkRequest(wrapped, Converter.getInstance());
    }

    @Override
    public String getUrl() {
        final String result = wrapped.uri().getPath();
        logger.debug("getUrl: {}", result);
        return result;
    }

    @Override
    public String getAbsoluteUrl() {
        final String result = wrapped.uri().toString();
        logger.debug("getAbsoluteUrl: {}", result);
        return result;
    }

    @Override
    public RequestMethod getMethod() {
        final RequestMethod result = RequestMethod.fromString(wrapped.method());
        logger.debug("getMethod: {}", result);
        return result;
    }

    @Override
    public String getScheme() {
        final String result = wrapped.uri().getScheme();
        logger.debug("getScheme: {}", result);
        return result;
    }

    @Override
    public String getHost() {
        final String result = wrapped.uri().getHost();
        logger.debug("getHost: {}", result);
        return result;
    }

    @Override
    public int getPort() {
        final int result = wrapped.uri().getPort();
        logger.debug("getPort: {}", result);
        return result;
    }

    @Override
    public String getClientIp() {
        logger.debug("getClientIp: 127.0.0.1");
        return "127.0.0.1";
    }

    private Optional<String> firstHeader(String s) {
        return wrapped.headers().firstValue(s);
    }

    @Override
    public String getHeader(String s) {
        final String result = firstHeader(s).orElse(null);
        logger.debug("getHeader: {}", result);
        return result;
    }

    @Override
    public HttpHeader header(String s) {
        final HttpHeader result;
        if (firstHeader(s).isEmpty()) {
            result = HttpHeader.empty(s);
        } else {
            result = HttpHeader.httpHeader(s, wrapped.headers().allValues(s).toArray(new String[0]));
        }
        logger.debug("header: {}", result);
        return result;
    }

    @Override
    public ContentTypeHeader contentTypeHeader() {
        ContentTypeHeader result = firstHeader(ContentTypeHeader.KEY)
                .map(ContentTypeHeader::new)
                .orElse(ContentTypeHeader.absent());
        logger.debug("contentTypeHeader: {}", result);
        return result;
    }

    @Override
    public HttpHeaders getHeaders() {
        HttpHeaders result = converter.fromJdk(wrapped.headers());
        logger.debug("getHeaders: {}", result);
        return result;
    }

    @Override
    public boolean containsHeader(String s) {
        boolean result = firstHeader(s).isPresent();
        logger.debug("containsHeader: {}", result);
        return result;
    }

    @Override
    public Set<String> getAllHeaderKeys() {
        Set<String> result = wrapped.headers().map().keySet();
        logger.debug("getAllHeaderKeys: {}", result);
        return result;
    }

    @Override
    public Map<String, Cookie> getCookies() {
        return Map.of(); // TODO cookies
    }

    @Override
    public QueryParameter queryParameter(String key) {
        Map<String, QueryParameter> query = Urls.splitQuery(wrapped.uri());
        QueryParameter result = query.get(key);
        logger.debug("queryParameter: {}", result);
        return result;
    }

    @Override
    public byte[] getBody() {
        final ByteBufferSubscriber subscriber = new ByteBufferSubscriber();
        wrapped.bodyPublisher().ifPresent(s -> s.subscribe(subscriber));
        return subscriber.result();
    }

    @Override
    public String getBodyAsString() {
        return new String(getBody(), UTF_8);
    }

    @Override
    public String getBodyAsBase64() {
        return Encoding.encodeBase64(getBody());
    }

    @Override
    public boolean isMultipart() {
        return false;
    }

    @Override
    public Collection<Part> getParts() {
        return Collections.emptySet();
    }

    @Override
    public Part getPart(String s) {
        return null;
    }

    @Override
    public boolean isBrowserProxyRequest() {
        return false;
    }

    @Override
    @SuppressWarnings("Guava")
    public com.google.common.base.Optional<Request> getOriginalRequest() {
        return com.google.common.base.Optional.absent();
    }
}
