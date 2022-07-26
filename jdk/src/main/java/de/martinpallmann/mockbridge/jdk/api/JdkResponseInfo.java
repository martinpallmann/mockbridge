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

import com.github.tomakehurst.wiremock.http.Response;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class JdkResponseInfo implements HttpResponse.ResponseInfo {

    private final HttpClient.Version version;
    private final Response response;
    private final Converter converter;

    public static HttpResponse.ResponseInfo responseInfo(HttpClient.Version version, Response response) {
        return new JdkResponseInfo(version, response, Converter.getInstance());
    }

    @Override
    public int statusCode() {
        return response.getStatus();
    }

    @Override
    public HttpHeaders headers() {
        return converter.fromWiremock(response.getHeaders());
    }

    @Override
    public HttpClient.Version version() {
        return version;
    }
}
