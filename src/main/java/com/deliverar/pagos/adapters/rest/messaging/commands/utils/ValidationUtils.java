package com.deliverar.pagos.adapters.rest.messaging.commands.utils;

import com.deliverar.pagos.domain.entities.Owner;
import com.deliverar.pagos.domain.usecases.owner.GetOwnerByEmail;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

/**
 * Utility class for common validations across commands.
 * Provides consistent validation patterns and error handling.
 */
@Slf4j
public class ValidationUtils {

    /**
     * Validates that an owner exists with the given email.
     * 
     * @param getOwnerByEmailUseCase The use case to get owner by email
     * @param email The email to validate
     * @return The owner if found
     * @throws IllegalArgumentException if owner not found
     */
    public static Owner validateOwnerExists(GetOwnerByEmail getOwnerByEmailUseCase, String email) {
        Optional<Owner> ownerOptional = getOwnerByEmailUseCase.get(email);
        if (ownerOptional.isEmpty()) {
            log.warn("Owner not found with email: {}", email);
            throw new IllegalArgumentException("Owner not found with email: " + email);
        }
        return ownerOptional.get();
    }

    /**
     * Validates that all required fields are present in the data.
     * 
     * @param data The data to validate
     * @param fields The required field names
     * @throws IllegalArgumentException if any required field is missing
     */
    public static void validateRequiredFields(Map<String, Object> data, String... fields) {
        for (String field : fields) {
            if (!data.containsKey(field) || data.get(field) == null) {
                log.warn("Required field missing: {}", field);
                throw new IllegalArgumentException("Required field missing: " + field);
            }
        }
    }

    /**
     * Parses a BigDecimal from the data map with a default value.
     * 
     * @param data The data map
     * @param field The field name
     * @param defaultValue The default value if field is not present
     * @return The parsed BigDecimal or default value
     */
    public static BigDecimal parseBigDecimal(Map<String, Object> data, String field, BigDecimal defaultValue) {
        if (!data.containsKey(field) || data.get(field) == null) {
            return defaultValue;
        }
        
        try {
            return new BigDecimal(data.get(field).toString());
        } catch (NumberFormatException e) {
            log.warn("Invalid number format for field {}: {}", field, data.get(field));
            throw new IllegalArgumentException("Invalid number format for field: " + field);
        }
    }

    /**
     * Validates that a BigDecimal value is positive.
     * 
     * @param value The value to validate
     * @param fieldName The field name for error messages
     * @throws IllegalArgumentException if value is not positive
     */
    public static void validatePositiveAmount(BigDecimal value, String fieldName) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Invalid {} value: {}", fieldName, value);
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
    }

    /**
     * Validates that an owner has sufficient balance for a transaction.
     * 
     * @param owner The owner to validate
     * @param requiredAmount The required amount
     * @param balanceType The type of balance (fiat/crypto)
     * @throws IllegalArgumentException if insufficient balance
     */
    public static void validateSufficientBalance(Owner owner, BigDecimal requiredAmount, String balanceType) {
        BigDecimal currentBalance = "fiat".equalsIgnoreCase(balanceType) 
            ? owner.getWallet().getFiatBalance() 
            : owner.getWallet().getCryptoBalance();
            
        if (currentBalance.compareTo(requiredAmount) < 0) {
            log.warn("Insufficient {} balance for owner {}: required {}, available {}", 
                    balanceType, owner.getEmail(), requiredAmount, currentBalance);
            throw new IllegalArgumentException("Insufficient " + balanceType + " balance");
        }
    }

    /**
     * Validates that an email has a valid format.
     * 
     * @param email The email to validate
     * @throws IllegalArgumentException if email format is invalid
     */
    public static void validateEmailFormat(String email) {
        if (email == null || !email.contains("@")) {
            log.warn("Invalid email format: {}", email);
            throw new IllegalArgumentException("Invalid email format");
        }
    }
} 