package com.deliverar.pagos.adapters.rest.messaging.commands;

import lombok.Data;

import java.util.List;

@Data
public class CommandResult {
    private boolean success;
    private String message;
    private Object data;
    private List<String> errors;

    public CommandResult(boolean success, String message, Object data, List<String> errors) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.errors = errors;
    }

    public static CommandResult buildSuccess(Object data) {
        return new CommandResult(true, "Success", data, null);
    }

    public static CommandResult buildSuccess(Object data, String message) {
        return new CommandResult(true, message, data, null);
    }

    public static CommandResult buildFailure(String message) {
        return new CommandResult(false, message, null, List.of(message));
    }

    public static CommandResult buildFailure(String message, List<String> errors) {
        return new CommandResult(false, message, null, errors);
    }
} 