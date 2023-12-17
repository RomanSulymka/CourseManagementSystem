package edu.sombra.coursemanagementsystem.security.config;

import edu.sombra.coursemanagementsystem.security.jwt.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;
    private final LogoutHandler logoutHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizeHttpRequests ->
                        authorizeHttpRequests
                                .requestMatchers("/api/v1/auth/**").permitAll()

                                // Course
                                .requestMatchers("/api/v1/course/create", "/api/v1/course/edit", "/api/v1/find-all-lessons/{id}").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.GET, "/api/v1/course/{id}", "/api/v1/course/find-all").hasAnyRole("ADMIN", "INSTRUCTOR", "STUDENT")
                                .requestMatchers(HttpMethod.PUT, "/api/v1/course").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.POST, "/api/v1/course/finish").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.GET, "/api/v1/course/user/{userId}").hasAnyRole("ADMIN", "INSTRUCTOR", "STUDENT")
                                .requestMatchers(HttpMethod.POST, "/api/v1/course/instructor/users").hasAnyRole("ADMIN", "INSTRUCTOR")
                                .requestMatchers(HttpMethod.DELETE, "/api/v1/course/{id}").hasRole("ADMIN")

                                // Course feedback
                                .requestMatchers(HttpMethod.POST, "/api/v1/feedback").hasAnyRole("ADMIN", "INSTRUCTOR")
                                .requestMatchers(HttpMethod.PUT, "/api/v1/feedback").hasAnyRole("ADMIN", "INSTRUCTOR")
                                .requestMatchers(HttpMethod.GET, "/api/v1/feedback").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.GET, "/api/v1/feedback/{id}").hasAnyRole("ADMIN", "INSTRUCTOR", "STUDENT")
                                .requestMatchers(HttpMethod.DELETE, "/api/v1/feedback/{id}").hasAnyRole("ADMIN", "INSTRUCTOR")

                                // Enrollment
                                .requestMatchers("/api/v1/enrollment/instructor").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.GET, "/api/v1/enrollment/{id}").hasRole("ADMIN")
                                .requestMatchers("/api/v1/enrollment/by-name").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/v1/enrollment").hasRole("ADMIN")
                                .requestMatchers("/api/v1/enrollment/user/apply").hasAnyRole("ADMIN", "STUDENT")
                                .requestMatchers(HttpMethod.DELETE, "/api/v1/enrollment/{id}").hasAnyRole("ADMIN")
                                .requestMatchers(HttpMethod.GET, "/api/v1/enrollment/user/{id}").hasAnyRole("ADMIN", "INSTRUCTOR", "STUDENT")

                                // File
                                .requestMatchers(HttpMethod.POST, "/api/v1/files/upload").hasAnyRole("ADMIN", "STUDENT")
                                .requestMatchers(HttpMethod.GET, "/api/v1/files/download/{fileId}").hasAnyRole("ADMIN", "INSTRUCTOR", "STUDENT")
                                .requestMatchers(HttpMethod.DELETE, "/api/v1/files/{fileId}").hasAnyRole("ADMIN", "INSTRUCTOR", "STUDENT")

                                // Homework
                                .requestMatchers(HttpMethod.PUT, "/api/v1/homework/mark").hasAnyRole("ADMIN", "INSTRUCTOR")
                                .requestMatchers(HttpMethod.GET, "/api/v1/homework/user/{userId}").hasAnyRole("ADMIN", "INSTRUCTOR")
                                .requestMatchers(HttpMethod.DELETE, "/api/v1/homework/{homeworkId}").hasAnyRole("ADMIN")
                                .requestMatchers(HttpMethod.GET, "/api/v1/homework/{homeworkId}").hasAnyRole("ADMIN", "INSTRUCTOR", "STUDENT")
                                .requestMatchers(HttpMethod.GET, "/api/v1/homework").hasAnyRole("ADMIN", "INSTRUCTOR", "STUDENT")
                                .requestMatchers(HttpMethod.POST, "/api/v1/homework").hasAnyRole("ADMIN", "INSTRUCTOR", "STUDENT")

                                // Lesson
                                .requestMatchers(HttpMethod.GET, "/api/v1/lesson/find-all").hasAnyRole("ADMIN", "INSTRUCTOR", "STUDENT")
                                .requestMatchers(HttpMethod.POST, "/api/v1/lesson/create").hasAnyRole("ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/v1/lesson/{id}").hasAnyRole("ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/v1/lesson/edit").hasAnyRole("ADMIN")
                                .requestMatchers(HttpMethod.GET, "/api/v1/lesson/{id}").hasAnyRole("ADMIN", "INSTRUCTOR", "STUDENT")
                                .requestMatchers(HttpMethod.GET, "/api/v1/lesson/find-all/{id}").hasAnyRole("ADMIN", "INSTRUCTOR", "STUDENT")

                                // User
                                .requestMatchers(HttpMethod.POST, "/api/v1/user/create").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/v1/user/update").hasAnyRole("ADMIN", "INSTRUCTOR", "STUDENT")
                                .requestMatchers(HttpMethod.POST, "/api/v1/user/assign-role").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/v1/user/reset-password").hasAnyRole("ADMIN", "INSTRUCTOR", "STUDENT")
                                .requestMatchers(HttpMethod.GET, "/api/v1/user/{id}").hasAnyRole("ADMIN", "INSTRUCTOR", "STUDENT")
                                .requestMatchers(HttpMethod.POST, "/api/v1/user/email").hasAnyRole("ADMIN", "INSTRUCTOR", "STUDENT")
                                .requestMatchers(HttpMethod.GET, "/api/v1/user/find-all").hasAnyRole("ADMIN", "INSTRUCTOR", "STUDENT")
                                .requestMatchers(HttpMethod.DELETE, "/api/v1/user/{id}").hasRole("ADMIN")

                                .anyRequest().authenticated()
                )
                .sessionManagement(authorizeRequests ->
                        authorizeRequests.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling
                                .accessDeniedHandler(accessDeniedHandler())
                                .authenticationEntryPoint(authenticationEntryPoint())
                )
                .logout(logout ->
                        logout.deleteCookies("remove")
                                .logoutUrl("/api/v1/auth/logout")
                                .permitAll()
                                .addLogoutHandler(logoutHandler)
                                .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext())
                );

        return http.build();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Access Denied");
        };
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Bad Request");
        };
    }
}
