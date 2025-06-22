package com.deliverar.pagos.adapters.rest.messaging.commands;

import com.deliverar.pagos.adapters.rest.messaging.events.EventType;
import com.deliverar.pagos.adapters.rest.messaging.events.IncomingEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class CommandManager {

    private final Map<EventType, Command> commands = new ConcurrentHashMap<>();

    public CommandManager(List<Command> commandList) {
        commandList.forEach(this::registerCommand);
    }

    public CommandResult executeCommand(IncomingEvent event) {
        try {
            EventType eventType = EventType.valueOf(event.getTopic());
            Command command = findCommand(eventType);

            if (command == null) {
                return CommandResult.buildFailure("No command found for event type: " + eventType);
            }

            return command.execute(event);

        } catch (Exception e) {
            log.error("Error executing command for event: {}", event.getTopic(), e);
            return CommandResult.buildFailure("Command execution failed: " + e.getMessage());
        }
    }

    public void registerCommand(Command command) {
        for (EventType eventType : EventType.values()) {
            if (command.canHandle(eventType)) {
                commands.put(eventType, command);
                log.info("Registered command {} for event type {}",
                        command.getClass().getSimpleName(), eventType);
            }
        }
    }

    public Command findCommand(EventType eventType) {
        return commands.get(eventType);
    }
} 