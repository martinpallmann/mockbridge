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
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ConverterTest {

    private final Converter converter = Converter.getInstance();

    @Test
    public void noHeaders() {
        HttpHeaders headers = HttpHeaders.noHeaders();
        assertEquals(
                Collections.emptyMap(),
                converter.fromWiremock(headers).map()
        );
    }

    @Test
    public void singleHeader() {
        HttpHeader header = new HttpHeader("Content-Type", "application/json");
        HttpHeaders headers = new HttpHeaders(header);
        assertEquals(
                Map.of("Content-Type", List.of("application/json")),
                converter.fromWiremock(headers).map()
        );
    }
}
