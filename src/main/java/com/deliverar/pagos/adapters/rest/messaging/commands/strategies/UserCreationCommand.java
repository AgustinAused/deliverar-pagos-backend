package com.deliverar.pagos.adapters.rest.messaging.commands.strategies;

import com.deliverar.pagos.adapters.rest.messaging.commands.BaseCommand;
import com.deliverar.pagos.adapters.rest.messaging.commands.CommandResult;
import com.deliverar.pagos.adapters.rest.messaging.events.EventType;
import com.deliverar.pagos.adapters.rest.messaging.events.IncomingEvent;
import com.deliverar.pagos.domain.dtos.CreateUserResponse;
import com.deliverar.pagos.domain.entities.Role;
import com.deliverar.pagos.domain.entities.User;
import com.deliverar.pagos.domain.usecases.user.CreateUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCreationCommand extends BaseCommand {
    
    private final CreateUser createUserUseCase;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public boolean canHandle(EventType eventType) {
        return EventType.USER_CREATION_REQUEST.equals(eventType);
    }
    
    @Override
    protected boolean validate(IncomingEvent event) {
        Map<String, Object> data = event.getData();
        return data != null && 
               data.containsKey("name") && 
               data.containsKey("email") &&
               data.containsKey("password");
    }
    
    @Override
    protected CommandResult process(IncomingEvent event) {
        try {
            Map<String, Object> data = event.getData();
            
            String name = (String) data.get("name");
            String email = (String) data.get("email");
            String password = (String) data.get("password");
            Role role = data.containsKey("role") ? Role.valueOf((String) data.get("role")) : Role.CORE;
            
            // Encode password
            String encodedPassword = passwordEncoder.encode(password);
            
            // Create user using the use case
            User user = createUserUseCase.create(name, email, encodedPassword, role);
            
            // Build response as Map
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("id", user.getId().toString());
            response.put("name", user.getName());
            response.put("email", user.getEmail());
            response.put("role", user.getRole().name());
            response.put("createdAt", user.getCreatedAt());
            
            // Add traceData if present in the request
            if (data.containsKey("traceData")) {
                response.put("traceData", data.get("traceData"));
            }
            
            return CommandResult.buildSuccess(response, "User created successfully");
            
        } catch (Exception e) {
            log.error("Error processing user creation command", e);
            return CommandResult.buildFailure("Failed to create user: " + e.getMessage());
        }
    }
} 