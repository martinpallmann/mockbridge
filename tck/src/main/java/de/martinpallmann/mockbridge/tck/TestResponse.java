package de.martinpallmann.mockbridge.tck;

import com.github.tomakehurst.wiremock.http.Body;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TestResponse {

    int status;
    Body body;

    public static TestResponse of(ResponseDefinition def) {

        return TestResponse
                .builder()
                .status(def.getStatus())
                .body(def.getReponseBody())
                .build();
    }
}
