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
     * Builds a response with specific fields from original data and automatically adds traceData.
     * 
     * @param originalData The original event data
     * @param fields The fields to include in the response
     * @return A new Map with the specified fields and traceData
     */
    public static Map<String, Object> buildResponse(Map<String, Object> originalData, String... fields) {
        Map<String, Object> response = new HashMap<>();
        
        // Add specified fields from original data
        for (String field : fields) {
            if (originalData.containsKey(field)) {
                response.put(field, originalData.get(field));
            }
        }
        
        // Automatically add traceData if present
        addTraceData(response, originalData);
        
        return response;
    }

    /**
     * Adds traceData to the response if present in the original data.
     * 
     * @param response The response map to add traceData to
     * @param originalData The original event data
     */
    public static void addTraceData(Map<String, Object> response, Map<String, Object> originalData) {
        if (originalData.containsKey("traceData")) {
            response.put("traceData", originalData.get("traceData"));
            log.debug("Added traceData to response");
        }
    }

    /**
     * Builds an error response with the given error message and traceData.
     * 
     * @param errorMessage The error message
     * @param originalData The original event data
     * @return A Map containing the error message and traceData
     */
    public static Map<String, Object> buildErrorResponse(String errorMessage, Map<String, Object> originalData) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", errorMessage);
        addTraceData(response, originalData);
        return response;
    }

    /**
     * Creates a new response map with the given key-value pairs and traceData.
     * 
     * @param originalData The original event data (for traceData)
     * @param keyValuePairs Alternating key-value pairs to add to the response
     * @return A new Map with the specified data and traceData
     */
    public static Map<String, Object> createResponse(Map<String, Object> originalData, Object... keyValuePairs) {
        Map<String, Object> response = new HashMap<>();
        
        // Add key-value pairs
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            if (i + 1 < keyValuePairs.length) {
                response.put(keyValuePairs[i].toString(), keyValuePairs[i + 1]);
            }
        }
        
        // Add traceData
        addTraceData(response, originalData);
        
        return response;
    }
} 