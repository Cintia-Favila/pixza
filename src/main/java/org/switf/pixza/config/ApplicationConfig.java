package org.switf.pixza.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.switf.pixza.models.UserModel;
import org.switf.pixza.repositories.UserJpaRepository;

import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final UserJpaRepository userRepository;

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CommandLineRunner createDefaultUser(UserJpaRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String defaultUsername = "Ulises";
            String defaultPassword = "admin";

            if (userRepository.findByUsername(defaultUsername).isEmpty()) {
                UserModel user = new UserModel();
                user.setUsername(defaultUsername);
                user.setPassword(passwordEncoder.encode(defaultPassword));
                user.setRoles(Set.of("ROLE_ADMIN"));
                userRepository.save(user);
                System.out.println("Usuario por defecto creado: " + defaultUsername + "/" + defaultPassword);
            }
        };
    }
}
