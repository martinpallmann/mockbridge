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
