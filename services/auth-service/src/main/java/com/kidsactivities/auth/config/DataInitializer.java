package com.kidsactivities.auth.config;

import com.kidsactivities.auth.entity.User;
import com.kidsactivities.auth.repository.UserRepository;
import com.kidsactivities.common.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            userRepository.save(User.builder()
                    .email("admin@kidsactivities.fr")
                    .password(passwordEncoder.encode("admin123"))
                    .firstName("Admin")
                    .lastName("System")
                    .role(Role.ADMIN)
                    .build());

            userRepository.save(User.builder()
                    .email("parent@example.com")
                    .password(passwordEncoder.encode("parent123"))
                    .firstName("Marie")
                    .lastName("Dupont")
                    .role(Role.PARENT)
                    .build());
        }
    }
}
