package com.exelynt.booking.config;

import com.exelynt.booking.model.Role;
import com.exelynt.booking.model.User;
import com.exelynt.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Ensures a default ADMIN account exists on startup, so there is always at least
 * one way to bootstrap the system (create resources, promote workflows, etc.)
 * without needing direct DB access. Credentials are configurable via
 * app.admin.* properties / ADMIN_USERNAME / ADMIN_PASSWORD env vars.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Override
    public void run(String... args) {
        if (userRepository.existsByUsername(adminUsername)) {
            return;
        }
        User admin = User.builder()
                .username(adminUsername)
                .password(passwordEncoder.encode(adminPassword))
                .email(adminEmail)
                .role(Role.ADMIN)
                .build();
        userRepository.save(admin);
        System.out.println("Seeded default ADMIN user -> username: " + adminUsername);
    }
}
