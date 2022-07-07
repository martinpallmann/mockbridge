package de.martinpallmann.mockbridge.jdk.api;

import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ConverterTest {

    private final Converter converter = new Converter();

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
