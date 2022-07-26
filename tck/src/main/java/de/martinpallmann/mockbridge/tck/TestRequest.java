package de.martinpallmann.mockbridge.tck;

import com.github.tomakehurst.wiremock.matching.MultiValuePattern;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import lombok.Builder;
import lombok.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Value
@Builder
public class TestRequest {

    String url;
    String method;
    Map<String, List<String>> headers;

    public String getAbsoluteUrl() {
        return "https://example.com" + url;
    }

    public static TestRequest of(RequestPattern pattern) {
        return TestRequest
                .builder()
                .url(pattern.getUrl())
                .method(pattern.getMethod().toString())
                .headers(convertHeaders(pattern.getHeaders()))
                .build();
    }

    private static Map<String, List<String>> convertHeaders(Map<String, MultiValuePattern> m) {
        if (m == null) {
            return null;
        }
        Map<String, List<String>> result = new HashMap<>();
        for (Map.Entry<String, MultiValuePattern> e : m.entrySet()) {
            List<String> values = new ArrayList<>();
            values.add(e.getValue().getValuePattern().getValue());
            result.put(e.getKey(), values);
        }
        return result;
    }
}
