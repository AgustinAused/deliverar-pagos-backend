package com.deliverar.pagos.adapters.rest.messaging.internal;

import com.deliverar.pagos.adapters.rest.messaging.core.dtos.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubEventListener {

    @EventListener
    public void onHubEvent(Event ev) {
        log.info("Hub event received: {}", ev);
        // ToDo: add event handling
    }
}
