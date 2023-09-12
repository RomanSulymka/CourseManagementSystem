package edu.sombra.coursemanagementsystem.security.config;

import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import edu.sombra.coursemanagementsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {
    private final UserRepository userRepository;

    @Bean
    public UserDetailsService userDetailsService() {
        return userRepository::findUserByEmail;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public CommandLineRunner defaultUsers() {
        return args -> {
            if (userRepository.findUserByEmail("admin@gmail.com") == null) {
                User admin = User.builder()
                        .firstName("admin")
                        .lastName("admin")
                        .email("admin@gmail.com")
                        .password(passwordEncoder().encode("adminPass"))
                        .role(RoleEnum.ADMIN)
                        .build();
                userRepository.create(admin);
            }

            if (userRepository.findUserByEmail("instructor@gmail.com") == null) {
                User instructor = User.builder()
                        .firstName("instructor")
                        .lastName("instructor")
                        .email("instructor@gmail.com")
                        .password(passwordEncoder().encode("instructorPass"))
                        .role(RoleEnum.INSTRUCTOR)
                        .build();
                userRepository.create(instructor);
            }

            if (userRepository.findUserByEmail("student@gmail.com") == null) {
                User student = User.builder()
                        .firstName("student")
                        .lastName("student")
                        .email("student@gmail.com")
                        .password(passwordEncoder().encode("studentPass"))
                        .role(RoleEnum.STUDENT)
                        .build();
                userRepository.create(student);
            }
        };
    }
}
