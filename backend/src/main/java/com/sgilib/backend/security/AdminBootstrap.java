package com.sgilib.backend.security;

import com.sgilib.backend.domain.AppUser;
import com.sgilib.backend.domain.UserRole;
import com.sgilib.backend.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminBootstrap(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (appUserRepository.existsByUsername("admin")) {
            return;
        }

        AppUser admin = new AppUser();
        admin.setUsername("admin");
        admin.setEmail("admin@sgilib.dev");
        admin.setPasswordHash(passwordEncoder.encode("Admin123!"));
        admin.setRole(UserRole.ADMIN);
        appUserRepository.save(admin);
    }
}
