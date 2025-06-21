package com.deliverar.pagos.adapters.rest.messaging;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HubPublisherTest {

    private MockWebServer mockWebServer;
    private HubPublisher publisher;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        String baseUrl = mockWebServer.url("").toString();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        publisher = new HubPublisher(baseUrl);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void publish_WhenHubReturns200_CompletesSuccessfully() {
        // Arrange: Hub responde 200 OK
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));
        HubPublish pub = new HubPublish("topic.test", Map.of("key", "value"));

        // Act & Assert: no lanza excepción
        assertDoesNotThrow(() -> publisher.publish(pub).block());
    }

    @Test
    void publish_WhenHubReturnsError_ThrowsException() {
        // Arrange: Hub responde 500
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        HubPublish pub = new HubPublish("topic.error", Map.of());

        // Act & Assert: lanza excepción con código 500 en el mensaje
        Exception ex = assertThrows(Exception.class, () -> publisher.publish(pub).block());
        assertTrue(ex.getMessage().contains("500"));
    }

    @Test
    void publish_SendsCorrectHttpRequest() throws InterruptedException {
        // Arrange: stub success
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));
        Map<String, Object> data = Map.of("field", 123);
        HubPublish pub = new HubPublish("topic.verify", data);

        // Act
        publisher.publish(pub).block();

        // Assert: verifica método, path y body JSON
        var recorded = mockWebServer.takeRequest();
        assertEquals("POST", recorded.getMethod());
        assertEquals("/hub/publish", recorded.getPath());
        String body = recorded.getBody().readUtf8();
        assertTrue(body.contains("\"topic\":\"topic.verify\""));
        assertTrue(body.contains("\"field\":123"));
    }
}
