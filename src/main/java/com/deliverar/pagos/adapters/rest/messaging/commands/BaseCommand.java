package com.deliverar.pagos.adapters.rest.messaging.commands;

import com.deliverar.pagos.adapters.rest.messaging.events.IncomingEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseCommand implements Command {

    @Override
    public CommandResult execute(IncomingEvent event) {
        try {
            log.info("Executing command for event: {}", event.getTopic());

            if (!validate(event)) {
                return CommandResult.buildFailure("Invalid event data");
            }

            return process(event);

        } catch (Exception e) {
            log.error("Error executing command for event: {}", event.getTopic(), e);
            return CommandResult.buildFailure("Internal error: " + e.getMessage());
        }
    }

    protected abstract boolean validate(IncomingEvent event);

    protected abstract CommandResult process(IncomingEvent event);
} 