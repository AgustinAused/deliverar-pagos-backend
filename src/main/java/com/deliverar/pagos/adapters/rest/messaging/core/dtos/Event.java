package com.deliverar.pagos.adapters.rest.messaging.core.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Event {
    private String topic;
    private Map<String, Object> payload;
}