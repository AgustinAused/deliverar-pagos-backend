package com.deliverar.pagos.adapters.rest.messaging;

import java.util.Map;

public record HubCallback(
        String event,
        Map<String, Object> data
) {
}
