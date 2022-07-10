---
title: MockBridge
---

<img src="gfx/logo.svg" alt="MockBridge" style="width: 300px;">

Mockbridge helps you to mock http requests
without the need to go over the wire.

## Usage

Assuming you have a Java http client in your code like this:
```java
import java.net.http.*;
import java.net.URI;

public class EchoClient {
    
    private final HttpClient httpClient;
    private final URI baseUri;
    
    public EchoClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        this.baseUri = URI.create("http://example.com/");
    }
    
    public String getEcho() {
        final HttpResponse<String> response = httpClient.send(
            HttpRequest.newBuilder(baseUri).build(),
            HttpResponse.BodyHandlers.ofString()
        );
        return response.body();
    }
}
```

Now you might want to test the client.
[Wiremock](https://wiremock.org) provides a method to do direct testing, which they describe here:
[https://wiremock.org/docs/running-without-http-server/](https://wiremock.org/docs/running-without-http-server/)

If it weren't for Mockbridge you'd need to write glue code
to map between the JDK HTTPClients request and response on one side
and the Wiremock representation on the other side.

Mockbridge does that for you.

```java
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static de.martinpallmann.mockbridge.jdk.MockBridge.httpClient;

public class MockBridgeTest {

    final EchoClient client = new EchoClient(httpClient());

    @Test
    public void test() {
        // WireMock stubbing. See https://wiremock.org/docs/stubbing/
        stubFor(get(anyUrl()).willReturn(ok().withBody("OK")));
        assertEquals("OK", client.getEcho());
    }
}
```

## Installation
Maven:
```xml
<dependencies>
    <dependency>
        <groupId>de.martinpallmann</groupId>
        <artifactId>mockbridge</artifactId>
        <version>${mockbridge.version}</version>
    </dependency>
    <dependency>
        <groupId>com.github.tomakehurst</groupId>
        <artifactId>wiremock-jre8</artifactId>
        <version>${wiremock.version}</version>
    </dependency>
</dependencies>
```

