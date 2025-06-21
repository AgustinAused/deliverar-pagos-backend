package com.deliverar.pagos.adapters.rest.messaging;

import java.util.Map;

public record HubPublish(
        String topic,
        Map<String, Object> data
) {
}
