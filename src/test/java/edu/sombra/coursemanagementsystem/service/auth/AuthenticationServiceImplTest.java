package edu.sombra.coursemanagementsystem.service.auth;


import edu.sombra.coursemanagementsystem.dto.auth.AuthenticationDTO;
import edu.sombra.coursemanagementsystem.dto.auth.AuthenticationResponse;
import edu.sombra.coursemanagementsystem.dto.auth.RegisterDTO;
import edu.sombra.coursemanagementsystem.entity.Token;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import edu.sombra.coursemanagementsystem.exception.UserAlreadyExistsException;
import edu.sombra.coursemanagementsystem.repository.token.TokenRepository;
import edu.sombra.coursemanagementsystem.security.jwt.JwtService;
import edu.sombra.coursemanagementsystem.service.UserService;
import edu.sombra.coursemanagementsystem.service.auth.impl.AuthenticateServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class AuthenticationServiceImplTest {
    @InjectMocks
    private AuthenticateServiceImpl authenticateService;

    @Mock
    private UserService userService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private TokenRepository tokenRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authenticateService = new AuthenticateServiceImpl(userService, passwordEncoder, jwtService, authenticationManager, tokenRepository);
    }

    private User createTestUser() {
         var user = new User();
        user.setId(1L);
        user.setEmail("user@email.com");
        user.setLastName("user");
        user.setFirstName("user");
        user.setRole(RoleEnum.STUDENT);
        return user;
    }

    @Test
    void testRegister_SuccessfulRegistration() {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setFirstName("John");
        registerDTO.setLastName("Doe");
        registerDTO.setEmail("john.doe@example.com");
        registerDTO.setPassword("password123");
        registerDTO.setRole(RoleEnum.STUDENT);

        when(userService.existsUserByEmail(registerDTO.getEmail())).thenReturn(false);

        User savedUser = new User();
        savedUser.setFirstName(registerDTO.getFirstName());
        savedUser.setLastName(registerDTO.getLastName());
        savedUser.setEmail(registerDTO.getEmail());
        savedUser.setPassword("encodedPassword");
        savedUser.setRole(registerDTO.getRole());

        when(userService.createUser(any(User.class))).thenReturn(savedUser);

        String jwtToken = "mockedJwtToken";
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn(jwtToken);

        AuthenticationResponse response = authenticateService.register(registerDTO);

        assertNotNull(response);
        assertEquals(jwtToken, response.getAccessToken());

        verify(tokenRepository, times(1)).save(any(Token.class));
    }

    @Test
    void testRegister_UserAlreadyExistsException() {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setEmail("existingUser@example.com");

        when(userService.existsUserByEmail(registerDTO.getEmail())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authenticateService.register(registerDTO));

        verify(userService, never()).createUser(any(User.class));
        verify(jwtService, never()).generateToken(any(User.class));
        verify(tokenRepository, never()).save(any(Token.class));
    }

    @Test
    void testAuthenticate_SuccessfulAuthentication() {
        AuthenticationDTO authenticationDTO = new AuthenticationDTO();
        authenticationDTO.setEmail("john.doe@example.com");
        authenticationDTO.setPassword("password123");

        when(userService.findUserByEmail(authenticationDTO.getEmail())).thenReturn(createTestUser());
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("mockedAccessToken");
        when(jwtService.generateRefreshToken(any(UserDetails.class))).thenReturn("mockedRefreshToken");

        AuthenticationResponse response = authenticateService.authenticate(authenticationDTO);

        assertNotNull(response);
        assertEquals("mockedAccessToken", response.getAccessToken());

        verify(tokenRepository, times(1)).save(any(Token.class));
    }

    @Test
    void testAuthenticate_InvalidCredentials() {
        AuthenticationDTO authenticationDTO = new AuthenticationDTO();
        authenticationDTO.setEmail("nonexistent.user@example.com");
        authenticationDTO.setPassword("invalidPassword");

        doThrow(new BadCredentialsException("Invalid credentials")).when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThrows(BadCredentialsException.class, () -> authenticateService.authenticate(authenticationDTO));

        verify(userService, never()).findUserByEmail(anyString());
        verify(jwtService, never()).generateToken(any(UserDetails.class));
        verify(jwtService, never()).generateRefreshToken(any(UserDetails.class));
        verify(tokenRepository, never()).save(any(Token.class));
    }

    @Test
    void testRefreshToken_SuccessfulRefresh() throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer mockRefreshToken");

        when(jwtService.extractUsername("mockRefreshToken")).thenReturn("user@example.com");
        when(userService.findUserByEmail("user@example.com")).thenReturn(createTestUser());
        when(jwtService.isTokenValid("mockRefreshToken", createTestUser())).thenReturn(true);
        when(jwtService.generateToken(createTestUser())).thenReturn("mockAccessToken");

        authenticateService.refreshToken(request, response);

        verify(tokenRepository, times(1)).findAllValidTokenByUser(anyLong());

        String expectedJson = "{\"access_token\":\"mockAccessToken\",\"refresh_token\":\"mockRefreshToken\"}";
        assertEquals(expectedJson, response.getContentAsString());
    }

    @Test
    void testRefreshToken_InvalidToken() throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("InvalidTokenFormat");

        authenticateService.refreshToken(request, response);

        verifyNoInteractions(userService, jwtService, tokenRepository);
        verify(response, never()).getOutputStream();
    }

    @Test
    void testAuthenticate_ValidUserTokensExist() {
        AuthenticationDTO authenticationDTO = new AuthenticationDTO();
        authenticationDTO.setEmail("john.doe@example.com");
        authenticationDTO.setPassword("password123");

        User testUser = new User();
        testUser.setEmail(authenticationDTO.getEmail());

        Token validToken1 = Token.builder().expired(false).revoked(false).build();
        Token validToken2 = Token.builder().expired(false).revoked(false).build();

        when(userService.findUserByEmail(authenticationDTO.getEmail())).thenReturn(testUser);
        when(jwtService.generateToken(testUser)).thenReturn("mockedAccessToken");
        when(jwtService.generateRefreshToken(testUser)).thenReturn("mockedRefreshToken");
        when(tokenRepository.findAllValidTokenByUser(testUser.getId())).thenReturn(Arrays.asList(validToken1, validToken2));

        AuthenticationResponse response = authenticateService.authenticate(authenticationDTO);

        assertNotNull(response);
        assertEquals("mockedAccessToken", response.getAccessToken());
        assertEquals("mockedRefreshToken", response.getRefreshToken());

        verify(tokenRepository, times(1)).saveAll(any());
        assertTrue(validToken1.isExpired() && validToken1.isRevoked());
        assertTrue(validToken2.isExpired() && validToken2.isRevoked());
    }
}
