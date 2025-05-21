package com.deliverar.pagos.infrastructure.bootstrap;

import com.deliverar.pagos.domain.entities.Role;
import com.deliverar.pagos.domain.entities.User;
import com.deliverar.pagos.domain.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AdminBootstrapRunner implements CommandLineRunner {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    @Value("${app.bootstrap.admin.email}")
    private String adminEmail;
    @Value("${app.bootstrap.admin.name}")
    private String name;
    @Value("${app.bootstrap.admin.password}")
    private String adminPass;

    public AdminBootstrapRunner(UserRepository userRepo, PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (userRepo.count() == 0) {
            User admin = new User();
            admin.setName(name);
            admin.setEmail(adminEmail);
            admin.setPasswordHash(encoder.encode(adminPass));
            admin.setRole(Role.ADMIN);
            userRepo.save(admin);
            log.info("Admin created");
        }
    }
}
