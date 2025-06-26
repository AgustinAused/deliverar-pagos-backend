package com.deliverar.pagos.adapters.rest.messaging.commands.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for building consistent responses across commands.
 * Handles traceData automatically and provides methods for different response types.
 */
@Slf4j
public class ResponseBuilder {

    /**
     * Adds traceData to the response if present in the original payload.
     *
     * @param response        The response map to add traceData to
     * @param originalPayload The original event payload
     */
    public static void addTraceData(Map<String, Object> response, Map<String, Object> originalPayload) {
        if (originalPayload.containsKey("traceData")) {
            response.put("traceData", originalPayload.get("traceData"));
            log.debug("Added traceData to response");
        }
    }

    /**
     * Creates a new response map with the given key-value pairs and traceData.
     *
     * @param originalPayload The original event payload (for traceData)
     * @param keyValuePairs   Alternating key-value pairs to add to the response
     * @return A new Map with the specified payload and traceData
     */
    public static Map<String, Object> createResponse(Map<String, Object> originalPayload, Object... keyValuePairs) {
        Map<String, Object> response = new HashMap<>();

        // Add key-value pairs
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            if (i + 1 < keyValuePairs.length) {
                response.put(keyValuePairs[i].toString(), keyValuePairs[i + 1]);
            }
        }

        // Add traceData
        addTraceData(response, originalPayload);

        return response;
    }
} 