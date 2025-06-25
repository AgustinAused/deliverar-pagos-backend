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
     * Builds a response with specific fields from original payload and automatically adds traceData.
     *
     * @param originalPayload The original event payload
     * @param fields          The fields to include in the response
     * @return A new Map with the specified fields and traceData
     */
    public static Map<String, Object> buildResponse(Map<String, Object> originalPayload, String... fields) {
        Map<String, Object> response = new HashMap<>();

        // Add specified fields from original payload
        for (String field : fields) {
            if (originalPayload.containsKey(field)) {
                response.put(field, originalPayload.get(field));
            }
        }

        // Automatically add traceData if present
        addTraceData(response, originalPayload);

        return response;
    }

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
     * Builds an error response with the given error message and traceData.
     *
     * @param errorMessage    The error message
     * @param originalPayload The original event payload
     * @return A Map containing the error message and traceData
     */
    public static Map<String, Object> buildErrorResponse(String errorMessage, Map<String, Object> originalPayload) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", errorMessage);
        addTraceData(response, originalPayload);
        return response;
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