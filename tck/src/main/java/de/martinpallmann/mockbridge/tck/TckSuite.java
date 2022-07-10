package de.martinpallmann.mockbridge.tck;


import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.stubbing.StubMappingCollection;
import de.martinpallmann.mockbridge.tck.api.MappingFactory;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class TckSuite {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected abstract TestResponse send(TestRequest request);

    @TestFactory
    Stream<DynamicTest> tests() throws IOException {
        return getMappings()
            .stream()
            .flatMap(s -> {

                final List<? extends StubMapping> mappings =
                        MappingFactory
                                .of("/mappings/" + s)
                                .getMappingOrMappings();

                return mappings.stream().map(mapping ->
                    dynamicTest(name(s, mapping.getName()), () -> {
                        final TestRequest request = TestRequest.of(mapping.getRequest());
                        assertEquals(TestResponse.of(mapping.getResponse()), send(request));
                    })
                );
            });
    }

    private static String name(String file, String mapping) {
        if (mapping == null) {
            return file;
        }
        return file + "/" + mapping;
    }


    private List<String> getMappings() throws IOException { // TODO maybe get these mappings from Wiremock
        List<String> filenames = new ArrayList<>();
        try(
            InputStream in = getClass().getResourceAsStream("/mappings");
            BufferedReader br = new BufferedReader(new InputStreamReader(in))
        ) {
            String resource;
            while ((resource = br.readLine()) != null) {
                filenames.add(resource);
            }
        }
        return filenames;
    }
}
