package com.deliverar.pagos.adapters.rest.messaging.internal;

import com.deliverar.pagos.adapters.rest.messaging.core.HubPublisher;
import com.deliverar.pagos.adapters.rest.messaging.core.dtos.Event;
import com.deliverar.pagos.adapters.rest.messaging.core.dtos.ImmutableEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubEventListener {
    private final HubPublisher hubPublisher;

    @EventListener
    public void onHubEvent(Event ev) {
        hubPublisher.publish(new ImmutableEvent(ev.getTopic(), ev.getData()))
                .doOnError(ex -> log.error("Fallo al publicar en Hub", ex))
                .subscribe();
    }
}
