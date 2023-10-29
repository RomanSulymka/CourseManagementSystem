package edu.sombra.coursemanagementsystem.security.config;

import edu.sombra.coursemanagementsystem.security.jwt.JwtAuthenticationFilter;
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
import org.springframework.security.web.SecurityFilterChain;
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
                                .requestMatchers("/api/v1/user/**").hasRole("ADMIN")

                                .requestMatchers("/api/v1/course/**").hasRole("ADMIN")
                                .requestMatchers("/api/v1/course/create", "/api/v1/course/edit", "/api/v1/find-all-lessons/{id}", "/api/v1/course/find-all").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.GET, "/api/v1/course/{{id}}").hasAnyRole("ADMIN", "INSTRUCTOR", "STUDENT")
                                .requestMatchers(HttpMethod.DELETE, "/api/v1/course/{{id}}").hasRole("ADMIN")
                                .requestMatchers("/api/v1/instructor/{instructorId}", "/api/v1/instructor/{instructorId}/{courseId}", "/finish/{studentId}/{courseId}").hasAnyRole("ADMIN", "INSTRUCTOR")
                                .requestMatchers("/api/v1/student/{studentId}", "/api/v1/student/lessons/{studentId}/{courseId}").hasAnyRole("ADMIN", "STUDENT")

                                .requestMatchers("/api/v1/enrollment/**").hasRole("ADMIN")
                                .requestMatchers("/api/v1/demo-controller").authenticated()
                                .anyRequest().authenticated()
                )
                .sessionManagement(authorizeRequests ->
                        authorizeRequests.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling
                                .accessDeniedPage("/errors/access-denied"))
                .logout(logout ->
                        logout.deleteCookies("remove")
                                .logoutUrl("/api/v1/auth/logout")
                                .permitAll()
                                .addLogoutHandler(logoutHandler)
                                .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext())
                );

        return http.build();
    }
}
