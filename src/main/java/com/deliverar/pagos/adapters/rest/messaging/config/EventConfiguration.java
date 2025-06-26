package com.deliverar.pagos.adapters.rest.messaging.config;

import com.deliverar.pagos.adapters.rest.messaging.commands.Command;
import com.deliverar.pagos.adapters.rest.messaging.commands.CommandManager;
import com.deliverar.pagos.adapters.rest.messaging.core.EventPublisher;
import com.deliverar.pagos.adapters.rest.messaging.core.EventRouter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class EventConfiguration {

    @Bean
    public CommandManager commandManager(List<Command> commands) {
        return new CommandManager(commands);
    }

    @Bean
    public EventRouter eventRouter(CommandManager commandManager, EventPublisher eventPublisher) {
        return new EventRouter(commandManager, eventPublisher);
    }
} 