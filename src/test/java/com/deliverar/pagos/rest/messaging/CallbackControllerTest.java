package com.deliverar.pagos.rest.messaging;

import com.deliverar.pagos.adapters.rest.messaging.CallbackController;
import com.deliverar.pagos.adapters.rest.messaging.HubCallback;
import com.deliverar.pagos.adapters.rest.messaging.HubEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CallbackControllerTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private CallbackController controller;

    @Captor
    private ArgumentCaptor<HubEvent> eventCaptor;

    @Test
    void verifySubscription_ShouldReturnChallenge() {
        // given
        String topic = "test-topic";
        String challenge = "test-challenge";

        // when
        ResponseEntity<String> response = controller.verifySubscription(topic, challenge);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(challenge);
    }

    //Todo: descomentar esto
//    @Test
//    void receiveEvent_ShouldPublishEventAndReturn204() {
//        // given
//        HubCallback callback = new HubCallback("test-event", Map.of("key", "value"));
//
//        // when
//        ResponseEntity<Void> response = controller.receiveEvent(callback);
//
//        // then
//        assertThat(response.getStatusCode().value()).isEqualTo(204);
//        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
//
//        HubEvent publishedEvent = eventCaptor.getValue();
//        assertThat(publishedEvent.getTopic()).isEqualTo("test-event");
//        assertThat(publishedEvent.getData()).isEqualTo(Map.of("key", "value"));
//    }

//    @Test
//    void receiveEvent_WhenExceptionOccurs_ShouldReturn200() {
//        // given
//        HubCallback callback = new HubCallback("test-event", Map.of("key", "value"));
//        doThrow(new RuntimeException("Test error"))
//            .when(applicationEventPublisher).publishEvent(any(HubEvent.class));
//
//        // when
//        ResponseEntity<Void> response = controller.receiveEvent(callback);
//
//        // then
//        assertThat(response.getStatusCode().value()).isEqualTo(200);
//    }

    // ToDo: borrar esto
    @Test
    void receiveEvent_WhenExceptionOccurs_ShouldReturn200() {
        // given
        HubCallback callback = new HubCallback("fail-testing", Map.of("key", "value"));

        // when
        ResponseEntity<Void> response = controller.receiveEvent(callback);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }
}
