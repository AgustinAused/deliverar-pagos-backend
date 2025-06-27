package com.deliverar.pagos.adapters.rest.messaging.commands;

import lombok.Data;

import java.util.List;

@Data
public class CommandResult {
    private boolean success;
    private String message;
    private Object payload;
    private List<String> errors;

    public CommandResult(boolean success, String message, Object payload, List<String> errors) {
        this.success = success;
        this.message = message;
        this.payload = payload;
        this.errors = errors;
    }

    public static CommandResult buildSuccess(Object payload) {
        return new CommandResult(true, "Success", payload, null);
    }

    public static CommandResult buildSuccess(Object payload, String message) {
        return new CommandResult(true, message, payload, null);
    }

    public static CommandResult buildFailure(String message) {
        return new CommandResult(false, message, null, List.of(message));
    }

    public static CommandResult buildFailure(String message, List<String> errors) {
        return new CommandResult(false, message, null, errors);
    }
} 