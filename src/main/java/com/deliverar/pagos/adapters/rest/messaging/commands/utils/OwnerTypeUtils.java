package com.deliverar.pagos.adapters.rest.messaging.commands.utils;

import com.deliverar.pagos.domain.entities.OwnerType;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public final class OwnerTypeUtils {

    private OwnerTypeUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Determines the OwnerType based on the traceData originModule.
     * 
     * @param data The event data containing traceData
     * @return OwnerType.NATURAL by default, OwnerType.LEGAL if originModule contains "marketplace"
     */
    public static OwnerType determineOwnerType(Map<String, Object> data) {
        if (data == null || !data.containsKey("traceData")) {
            return OwnerType.NATURAL;
        }

        Object traceDataObj = data.get("traceData");
        if (!(traceDataObj instanceof Map)) {
            return OwnerType.NATURAL;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> traceData = (Map<String, Object>) traceDataObj;
        
        if (!traceData.containsKey("originModule")) {
            return OwnerType.NATURAL;
        }

        String originModule = (String) traceData.get("originModule");
        if (originModule == null) {
            return OwnerType.NATURAL;
        }

        if (originModule.contains("marketplace")) {
            log.debug("Determined OwnerType.LEGAL for originModule: {}", originModule);
            return OwnerType.LEGAL;
        }

        return OwnerType.NATURAL;
    }
} 