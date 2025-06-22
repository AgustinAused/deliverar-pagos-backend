# Ejemplo de Implementación - Event-Driven Architecture

Este documento muestra ejemplos concretos de implementación de la arquitectura basada en eventos para el sistema de pagos de Deliverar.

## 1. Estructura de Eventos

### EventType.java

```java
package com.deliverar.pagos.adapters.rest.messaging.events;

public enum EventType {
    // Eventos de Entrada
    USER_CREATION_REQUEST,
    USER_DELETION_REQUEST,
    GET_BALANCES_REQUEST,
    GET_USER_FIAT_TRANSACTIONS_REQUEST,
    GET_USER_CRYPTO_TRANSACTIONS_REQUEST,
    FIAT_DEPOSIT_REQUEST,
    FIAT_WITHDRAWAL_REQUEST,
    FIAT_PAYMENT_REQUEST,
    CRYPTO_PAYMENT_REQUEST,
    BUY_CRYPTO_REQUEST,
    SELL_CRYPTO_REQUEST,
    GET_ALL_FIAT_TRANSACTIONS_REQUEST,
    GET_ALL_CRYPTO_TRANSACTIONS_REQUEST,

    // Eventos de Salida
    USER_CREATION_RESPONSE,
    USER_DELETION_RESPONSE,
    GET_BALANCES_RESPONSE,
    GET_USER_FIAT_TRANSACTIONS_RESPONSE,
    GET_USER_CRYPTO_TRANSACTIONS_RESPONSE,
    FIAT_DEPOSIT_RESPONSE,
    FIAT_WITHDRAWAL_RESPONSE,
    FIAT_PAYMENT_RESPONSE,
    CRYPTO_PAYMENT_RESPONSE,
    BUY_CRYPTO_RESPONSE,
    SELL_CRYPTO_RESPONSE,
    GET_ALL_FIAT_TRANSACTIONS_RESPONSE,
    GET_ALL_CRYPTO_TRANSACTIONS_RESPONSE,
    ERROR_RESPONSE
}
```

### IncomingEvent.java

```java
package com.deliverar.pagos.adapters.rest.messaging.events;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class IncomingEvent extends Event {
    private String correlationId;
    private String source;

    public IncomingEvent(String topic, Map<String, Object> data, String correlationId, String source) {
        super(topic, data);
        this.correlationId = correlationId;
        this.source = source;
        this.timestamp = LocalDateTime.now();
    }

    public boolean validate() {
        return topic != null && !topic.isEmpty() &&
               correlationId != null && !correlationId.isEmpty() &&
               source != null && !source.isEmpty();
    }
}
```

### OutgoingEvent.java

```java
package com.deliverar.pagos.adapters.rest.messaging.events;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class OutgoingEvent extends Event {
    private String correlationId;
    private String target;
    private EventStatus status;

    public OutgoingEvent(String topic, Map<String, Object> data, String correlationId, String target, EventStatus status) {
        super(topic, data);
        this.correlationId = correlationId;
        this.target = target;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    public static OutgoingEvent buildResponse(IncomingEvent incomingEvent, EventType responseType, Object data, EventStatus status) {
        return new OutgoingEvent(
            responseType.name(),
            Map.of("data", data),
            incomingEvent.getCorrelationId(),
            incomingEvent.getSource(),
            status
        );
    }
}
```

## 2. Comandos

### Command.java

```java
package com.deliverar.pagos.adapters.rest.messaging.commands;

import com.deliverar.pagos.adapters.rest.messaging.events.EventType;
import com.deliverar.pagos.adapters.rest.messaging.events.IncomingEvent;

public interface Command {
    CommandResult execute(IncomingEvent event);
    boolean canHandle(EventType eventType);
}
```

### BaseCommand.java

```java
package com.deliverar.pagos.adapters.rest.messaging.commands;

import com.deliverar.pagos.adapters.rest.messaging.events.EventType;
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
```

### BuyCryptoCommand.java

```java
package com.deliverar.pagos.adapters.rest.messaging.commands.strategies;

import com.deliverar.pagos.adapters.crypto.service.DeliverCoinService;
import com.deliverar.pagos.adapters.rest.messaging.commands.BaseCommand;
import com.deliverar.pagos.adapters.rest.messaging.commands.CommandResult;
import com.deliverar.pagos.adapters.rest.messaging.events.EventType;
import com.deliverar.pagos.adapters.rest.messaging.events.IncomingEvent;
import com.deliverar.pagos.domain.dtos.BuyCryptoRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BuyCryptoCommand extends BaseCommand {

    private final DeliverCoinService cryptoService;

    @Override
    public boolean canHandle(EventType eventType) {
        return EventType.BUY_CRYPTO_REQUEST.equals(eventType);
    }

    @Override
    protected boolean validate(IncomingEvent event) {
        Map<String, Object> data = event.getData();
        return data != null &&
               data.containsKey("email") &&
               data.containsKey("amount") &&
               data.containsKey("fiatAmount");
    }

    @Override
    protected CommandResult process(IncomingEvent event) {
        try {
            Map<String, Object> data = event.getData();

            BuyCryptoRequest request = BuyCryptoRequest.builder()
                .email((String) data.get("email"))
                .amount(new BigDecimal(data.get("amount").toString()))
                .fiatAmount(new BigDecimal(data.get("fiatAmount").toString()))
                .build();

            UUID transactionId = cryptoService.buyCrypto(request);

            return CommandResult.buildSuccess(Map.of(
                "transactionId", transactionId.toString(),
                "status", "PENDING"
            ));

        } catch (Exception e) {
            log.error("Error processing buy crypto command", e);
            return CommandResult.buildFailure("Failed to process buy crypto: " + e.getMessage());
        }
    }
}
```

## 3. Event Router

### EventRouter.java

```java
package com.deliverar.pagos.adapters.rest.messaging.core;

import com.deliverar.pagos.adapters.rest.messaging.commands.CommandManager;
import com.deliverar.pagos.adapters.rest.messaging.events.EventType;
import com.deliverar.pagos.adapters.rest.messaging.events.IncomingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventRouter {

    private final Map<EventType, EventHandler> handlers = new ConcurrentHashMap<>();
    private final CommandManager commandManager;
    private final EventPublisher eventPublisher;

    public void routeEvent(IncomingEvent event) {
        try {
            EventType eventType = EventType.valueOf(event.getTopic());

            if (!event.validate()) {
                log.error("Invalid event received: {}", event);
                return;
            }

            EventHandler handler = handlers.get(eventType);
            if (handler != null) {
                handler.handle(event);
            } else {
                // Use default handler
                DefaultEventHandler defaultHandler = new DefaultEventHandler(commandManager, eventPublisher);
                defaultHandler.handle(event);
            }

        } catch (IllegalArgumentException e) {
            log.error("Unknown event type: {}", event.getTopic());
        } catch (Exception e) {
            log.error("Error routing event: {}", event.getTopic(), e);
        }
    }

    public void registerHandler(EventType eventType, EventHandler handler) {
        handlers.put(eventType, handler);
        log.info("Registered handler for event type: {}", eventType);
    }
}
```

## 4. Event Handler

### DefaultEventHandler.java

```java
package com.deliverar.pagos.adapters.rest.messaging.core;

import com.deliverar.pagos.adapters.rest.messaging.commands.CommandManager;
import com.deliverar.pagos.adapters.rest.messaging.commands.CommandResult;
import com.deliverar.pagos.adapters.rest.messaging.events.EventType;
import com.deliverar.pagos.adapters.rest.messaging.events.EventStatus;
import com.deliverar.pagos.adapters.rest.messaging.events.IncomingEvent;
import com.deliverar.pagos.adapters.rest.messaging.events.OutgoingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class DefaultEventHandler implements EventHandler {

    private final CommandManager commandManager;
    private final EventPublisher eventPublisher;

    @Override
    public void handle(IncomingEvent event) {
        try {
            log.info("Handling event: {}", event.getTopic());

            CommandResult result = commandManager.executeCommand(event);

            if (result.isSuccess()) {
                publishSuccessResponse(event, result);
            } else {
                publishErrorResponse(event, result.getMessage());
            }

        } catch (Exception e) {
            log.error("Error handling event: {}", event.getTopic(), e);
            publishErrorResponse(event, "Internal server error");
        }
    }

    private void publishSuccessResponse(IncomingEvent event, CommandResult result) {
        EventType responseType = getResponseType(event.getTopic());
        OutgoingEvent response = OutgoingEvent.buildResponse(
            event,
            responseType,
            result.getData(),
            EventStatus.SUCCESS
        );
        eventPublisher.publish(response);
    }

    private void publishErrorResponse(IncomingEvent event, String errorMessage) {
        OutgoingEvent response = OutgoingEvent.buildResponse(
            event,
            EventType.ERROR_RESPONSE,
            Map.of("error", errorMessage),
            EventStatus.FAILURE
        );
        eventPublisher.publish(response);
    }

    private EventType getResponseType(String requestTopic) {
        return EventType.valueOf(requestTopic.replace("_REQUEST", "_RESPONSE"));
    }

    @Override
    public boolean canHandle(EventType eventType) {
        return true; // Default handler can handle all events
    }
}
```

## 5. Command Manager

### CommandManager.java

```java
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
```

## 6. Event Publisher

### EventPublisher.java

```java
package com.deliverar.pagos.adapters.rest.messaging.core;

import com.deliverar.pagos.adapters.rest.messaging.core.HubPublisher;
import com.deliverar.pagos.adapters.rest.messaging.core.dtos.ImmutableEvent;
import com.deliverar.pagos.adapters.rest.messaging.events.EventType;
import com.deliverar.pagos.adapters.rest.messaging.events.EventStatus;
import com.deliverar.pagos.adapters.rest.messaging.events.IncomingEvent;
import com.deliverar.pagos.adapters.rest.messaging.events.OutgoingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final HubPublisher hubPublisher;

    public void publish(OutgoingEvent event) {
        try {
            log.info("Publishing event: {}", event.getTopic());

            ImmutableEvent hubEvent = ImmutableEvent.builder()
                .topic(event.getTopic())
                .data(event.getData())
                .build();

            hubPublisher.publish(hubEvent)
                .doOnSuccess(v -> log.info("Event published successfully: {}", event.getTopic()))
                .doOnError(e -> log.error("Failed to publish event: {}", event.getTopic(), e))
                .subscribe();

        } catch (Exception e) {
            log.error("Error publishing event: {}", event.getTopic(), e);
        }
    }

    public void publishError(IncomingEvent incomingEvent, String errorMessage) {
        OutgoingEvent errorEvent = OutgoingEvent.buildResponse(
            incomingEvent,
            EventType.ERROR_RESPONSE,
            Map.of("error", errorMessage),
            EventStatus.FAILURE
        );
        publish(errorEvent);
    }
}
```

## 7. Configuración

### EventConfiguration.java

```java
package com.deliverar.pagos.adapters.rest.messaging.config;

import com.deliverar.pagos.adapters.rest.messaging.commands.Command;
import com.deliverar.pagos.adapters.rest.messaging.commands.CommandManager;
import com.deliverar.pagos.adapters.rest.messaging.core.EventRouter;
import com.deliverar.pagos.adapters.rest.messaging.core.EventPublisher;
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
```

## 8. Uso en CallbackController

### CallbackController actualizado

```java
@PostMapping
public ResponseEntity<Void> receiveEvent(@RequestBody ImmutableEvent event) {
    try {
        log.info("Evento recibido: {}", event);

        // Convertir a IncomingEvent
        IncomingEvent incomingEvent = new IncomingEvent(
            event.topic(),
            event.data(),
            generateCorrelationId(),
            "external-hub"
        );

        // Enrutar evento
        eventRouter.routeEvent(incomingEvent);

        return ResponseEntity.noContent().build();
    } catch (Exception ex) {
        log.error("Error procesando evento del hub", ex);
        return ResponseEntity.ok().build();
    }
}

private String generateCorrelationId() {
    return UUID.randomUUID().toString();
}
```

## Ventajas de esta Implementación

1. **Desacoplamiento**: Cada comando maneja su propia lógica de negocio
2. **Extensibilidad**: Fácil agregar nuevos comandos sin modificar código existente
3. **Testabilidad**: Cada componente puede ser testeado independientemente
4. **Mantenibilidad**: Lógica clara y separada por responsabilidades
5. **Escalabilidad**: Comandos pueden ser ejecutados de forma asíncrona si es necesario
