package edu.sombra.coursemanagementsystem.service.auth.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.sombra.coursemanagementsystem.dto.auth.AuthenticationDTO;
import edu.sombra.coursemanagementsystem.dto.auth.AuthenticationResponse;
import edu.sombra.coursemanagementsystem.dto.auth.RegisterDTO;
import edu.sombra.coursemanagementsystem.entity.Token;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.TokenType;
import edu.sombra.coursemanagementsystem.exception.UserAlreadyExistsException;
import edu.sombra.coursemanagementsystem.repository.token.TokenRepository;
import edu.sombra.coursemanagementsystem.security.jwt.JwtService;
import edu.sombra.coursemanagementsystem.service.UserService;
import edu.sombra.coursemanagementsystem.service.auth.AuthenticateService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticateServiceImpl implements AuthenticateService {
    private static final String JWT_HEADER = "Bearer ";

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenRepository tokenRepository;

    @Override
    public AuthenticationResponse register(RegisterDTO registerDTO) {
        if (userService.existsUserByEmail(registerDTO.getEmail())) {
            throw new UserAlreadyExistsException(registerDTO.getEmail());
        } else {
            var user = new User();
            user.setFirstName(registerDTO.getFirstName());
            user.setLastName(registerDTO.getLastName());
            user.setEmail(registerDTO.getEmail());
            user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
            user.setRole(registerDTO.getRole());

            var savedUser = userService.createUser(user);
            var jwtToken = jwtService.generateToken(user);
            var refreshedToken = jwtService.generateToken(user);
            saveUserToken(savedUser, jwtToken);
            return AuthenticationResponse.builder()
                    .accessToken(jwtToken)
                    .refreshToken(refreshedToken)
                    .build();
        }
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationDTO authenticationDTO) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticationDTO.getEmail(),
                        authenticationDTO.getPassword()
                )
        );

        var user = userService.findUserByEmail(authenticationDTO.getEmail());
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;
        if (authHeader == null || !authHeader.startsWith(JWT_HEADER)) {
            return;
        }
        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail != null) {
            var user = this.userService.findUserByEmail(userEmail);
            if (jwtService.isTokenValid(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user);
                revokeAllUserTokens(user);
                saveUserToken(user, accessToken);
                var authResponse = AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.writeValue(response.getOutputStream(), authResponse);
            }
        }
    }

    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .expired(false)
                .revoked(false)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty()) {
            return;
        }
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }
}
