package com.deliverar.pagos.adapters.rest.messaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HubEventListenerTest {

    @Mock
    private HubPublisher hubPublisher;

    private HubEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new HubEventListener(hubPublisher);
    }

    @Test
    void onHubEvent_WhenPublishSucceeds_ShouldInvokeHubPublisher() {
        // Arrange
        HubEvent event = new HubEvent("test.topic", Map.of("key", "value"));
        when(hubPublisher.publish(any(HubPublish.class))).thenReturn(Mono.empty());

        // Act
        listener.onHubEvent(event);

        // Assert
        ArgumentCaptor<HubPublish> captor = ArgumentCaptor.forClass(HubPublish.class);
        verify(hubPublisher, times(1)).publish(captor.capture());
        HubPublish published = captor.getValue();
        assert published.topic().equals("test.topic");
        assert published.data().equals(event.getData());
    }

    @Test
    void onHubEvent_WhenPublishFails_ShouldNotThrowAndStillInvokePublisher() {
        // Arrange
        HubEvent event = new HubEvent("error.topic", Map.of());
        when(hubPublisher.publish(any(HubPublish.class))).thenReturn(Mono.error(new RuntimeException("fail")));

        // Act & Assert: no exception from listener
        assertDoesNotThrow(() -> listener.onHubEvent(event));
        verify(hubPublisher, times(1)).publish(any(HubPublish.class));
    }
}