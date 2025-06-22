package com.deliverar.pagos.adapters.rest.messaging.core.dtos;

import java.util.Map;

public record ImmutableEvent(
        String topic,
        Map<String, Object> data
) {
}
