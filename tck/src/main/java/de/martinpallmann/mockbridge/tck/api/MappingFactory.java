package de.martinpallmann.mockbridge.tck.api;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.stubbing.StubMappingCollection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public final class MappingFactory {

    public static StubMappingCollection of(String fileName) {
        try (
                final InputStream is = MappingFactory.class.getResourceAsStream(fileName);
                final InputStreamReader ir = new InputStreamReader(is);
                final BufferedReader br = new BufferedReader(ir);
        ) {
            final String json = br.lines().collect(Collectors.joining("\n"));
            return Json.read(json, StubMappingCollection.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
