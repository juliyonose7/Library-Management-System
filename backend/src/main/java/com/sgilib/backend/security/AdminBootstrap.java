package com.sgilib.backend.security;

import com.sgilib.backend.domain.AppUser;
import com.sgilib.backend.domain.UserRole;
import com.sgilib.backend.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminEmail;
    private final String adminPassword;

    public AdminBootstrap(AppUserRepository appUserRepository,
                          PasswordEncoder passwordEncoder,
                          @Value("${app.bootstrap.admin.username:admin}") String adminUsername,
                          @Value("${app.bootstrap.admin.email:admin@sgilib.dev}") String adminEmail,
                          @Value("${app.bootstrap.admin.password:}") String adminPassword) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        if (adminPassword == null || adminPassword.isBlank()) {
            System.out.println("[SECURITY] Admin bootstrap skipped: APP_BOOTSTRAP_ADMIN_PASSWORD is not set.");
            return;
        }

        if (appUserRepository.existsByUsername(adminUsername)) {
            return;
        }

        AppUser admin = new AppUser();
        admin.setUsername(adminUsername);
        admin.setEmail(adminEmail);
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.setRole(UserRole.ADMIN);
        appUserRepository.save(admin);
    }
}
