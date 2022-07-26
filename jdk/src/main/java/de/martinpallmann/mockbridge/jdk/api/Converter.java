package de.martinpallmann.mockbridge.jdk.api;

import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;

import java.util.*;

public interface Converter {

    java.net.http.HttpHeaders fromWiremock(HttpHeaders headers);

    HttpHeaders fromJdk(java.net.http.HttpHeaders headers);

    static Converter getInstance() {
        return Impl.instance;
    }

    final class Impl implements Converter {

        private final static Converter instance = new Impl();

        public java.net.http.HttpHeaders fromWiremock(HttpHeaders headers) {
            Objects.requireNonNull(headers);
            final Map<String, List<String>> headerMap = new HashMap<>();
            for (HttpHeader header : headers.all()) {
                headerMap.put(header.key(), header.values());
            }
            return java.net.http.HttpHeaders.of(headerMap, (a, b) -> true);
        }

        public HttpHeaders fromJdk(java.net.http.HttpHeaders headers) {
            Objects.requireNonNull(headers);
            final List<HttpHeader> result = new ArrayList<>();
            for (Map.Entry<String, List<String>> entry : headers.map().entrySet()) {
                result.add(HttpHeader.httpHeader(entry.getKey(), entry.getValue().toArray(new String[0])));
            }
            return new HttpHeaders(result);
        }
    }
}
