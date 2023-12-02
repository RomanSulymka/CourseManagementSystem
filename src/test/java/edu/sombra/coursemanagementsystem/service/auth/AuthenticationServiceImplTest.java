package edu.sombra.coursemanagementsystem.service.auth;


import edu.sombra.coursemanagementsystem.dto.auth.AuthenticationDTO;
import edu.sombra.coursemanagementsystem.dto.auth.AuthenticationResponse;
import edu.sombra.coursemanagementsystem.dto.auth.RegisterDTO;
import edu.sombra.coursemanagementsystem.dto.user.CreateUserDTO;
import edu.sombra.coursemanagementsystem.dto.user.UserResponseDTO;
import edu.sombra.coursemanagementsystem.entity.Token;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import edu.sombra.coursemanagementsystem.exception.UserAlreadyExistsException;
import edu.sombra.coursemanagementsystem.mapper.UserMapper;
import edu.sombra.coursemanagementsystem.repository.token.TokenRepository;
import edu.sombra.coursemanagementsystem.security.jwt.JwtService;
import edu.sombra.coursemanagementsystem.service.UserService;
import edu.sombra.coursemanagementsystem.service.auth.impl.AuthenticateServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import java.util.stream.Stream;

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
    @Mock
    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authenticateService = new AuthenticateServiceImpl(userService, userMapper, passwordEncoder, jwtService, authenticationManager, tokenRepository);
    }

    private static Stream<Arguments> provideRegisterDTOs() {
        RegisterDTO dto1 = new RegisterDTO();
        dto1.setFirstName("John");
        dto1.setLastName("Doe");
        dto1.setEmail("john.doe@example.com");
        dto1.setPassword("password123");
        dto1.setRole(RoleEnum.STUDENT);

        RegisterDTO dto2 = new RegisterDTO();
        dto2.setFirstName("Jane");
        dto2.setLastName("Doe");
        dto2.setEmail("jane.doe@example.com");
        dto2.setPassword("password456");
        dto2.setRole(RoleEnum.STUDENT);

        return Stream.of(
                Arguments.of(dto1, "mockedJwtToken"),
                Arguments.of(dto2, "mockedJwtToken2")
        );
    }

    private static Stream<Arguments> provideRegisterEmails() {
        return Stream.of(
                Arguments.of("existingUser1@example.com"),
                Arguments.of("existingUser2@gmail.com")
        );
    }

    private static Stream<Arguments> provideAuthenticationDTOs() {
        AuthenticationDTO dto1 = new AuthenticationDTO();
        dto1.setEmail("john.doe@example.com");
        dto1.setPassword("password123");

        AuthenticationDTO dto2 = new AuthenticationDTO();
        dto2.setEmail("jane.doe@example.com");
        dto2.setPassword("password456");

        return Stream.of(
                Arguments.of(dto1, "mockedAccessToken", "mockedRefreshToken"),
                Arguments.of(dto2, "mockedAccessToken2", "mockedRefreshToken2")
        );
    }

    private static Stream<AuthenticationDTO> provideAuthenticationDTOsForInvalidCredentialsTest() {
        AuthenticationDTO dto1 = new AuthenticationDTO();
        dto1.setEmail("nonexistent.user1@example.com");
        dto1.setPassword("invalidPassword1");

        AuthenticationDTO dto2 = new AuthenticationDTO();
        dto2.setEmail("nonexistent.user2@example.com");
        dto2.setPassword("invalidPassword2");

        return Stream.of(dto1, dto2);
    }

    private static Stream<Arguments> provideRefreshTokenTestData() {
        return Stream.of(
                Arguments.of("Bearer mockRefreshToken1", "user1@example.com", "mockAccessToken1"),
                Arguments.of("Bearer mockRefreshToken2", "user2@example.com", "mockAccessToken2")
        );
    }

    private UserResponseDTO createTestUserResponse() {
        var user = new UserResponseDTO();
        user.setId(1L);
        user.setEmail("user@email.com");
        user.setLastName("user");
        user.setFirstName("user");
        user.setRole(RoleEnum.STUDENT);
        return user;
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

    private CreateUserDTO createTestUserDTO() {
        var user = new CreateUserDTO();
        user.setEmail("user@email.com");
        user.setLastName("user");
        user.setFirstName("user");
        user.setRole(RoleEnum.STUDENT);
        return user;
    }

    @ParameterizedTest
    @MethodSource("provideRegisterDTOs")
    void testRegister_SuccessfulRegistration(RegisterDTO registerDTO, String expectedJwtToken) {
        when(userService.existsUserByEmail(registerDTO.getEmail())).thenReturn(false);

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setFirstName(registerDTO.getFirstName());
        savedUser.setLastName(registerDTO.getLastName());
        savedUser.setEmail(registerDTO.getEmail());
        savedUser.setPassword("encodedPassword");
        savedUser.setRole(registerDTO.getRole());

        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setId(1L);
        userResponseDTO.setFirstName(registerDTO.getFirstName());
        userResponseDTO.setLastName(registerDTO.getLastName());
        userResponseDTO.setEmail(registerDTO.getEmail());
        userResponseDTO.setRole(registerDTO.getRole());

        when(userMapper.mapToDTO(any(User.class))).thenReturn(createTestUserDTO());
        when(userService.createUser(any(CreateUserDTO.class))).thenReturn(userResponseDTO);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn(expectedJwtToken);

        AuthenticationResponse response = authenticateService.register(registerDTO);

        assertNotNull(response);
        assertEquals(expectedJwtToken, response.getAccessToken());

        verify(tokenRepository, times(1)).save(any(Token.class));
    }

    @ParameterizedTest
    @MethodSource("provideRegisterEmails")
    void testRegister_UserAlreadyExistsException(String email) {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setEmail(email);

        when(userService.existsUserByEmail(registerDTO.getEmail())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authenticateService.register(registerDTO));

        verify(userService, never()).createUser(any(CreateUserDTO.class));
        verify(jwtService, never()).generateToken(any(User.class));
        verify(tokenRepository, never()).save(any(Token.class));
    }

    @ParameterizedTest
    @MethodSource("provideAuthenticationDTOs")
    void testAuthenticate_SuccessfulAuthentication(AuthenticationDTO authenticationDTO, String expectedAccessToken, String expectedRefreshToken) {
        when(userService.findUserByEmail(authenticationDTO.getEmail())).thenReturn(createTestUserResponse());
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn(expectedAccessToken);
        when(jwtService.generateRefreshToken(any(UserDetails.class))).thenReturn(expectedRefreshToken);
        when(userMapper.fromResponseDTO(any(UserResponseDTO.class))).thenReturn(createTestUser());
        AuthenticationResponse response = authenticateService.authenticate(authenticationDTO);

        assertNotNull(response);
        assertEquals(expectedAccessToken, response.getAccessToken());

        verify(tokenRepository, times(1)).save(any(Token.class));
    }

    @ParameterizedTest
    @MethodSource("provideAuthenticationDTOsForInvalidCredentialsTest")
    void testAuthenticate_InvalidCredentials(AuthenticationDTO authenticationDTO) {

        doThrow(new BadCredentialsException("Invalid credentials")).when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThrows(BadCredentialsException.class, () -> authenticateService.authenticate(authenticationDTO));

        verify(userService, never()).findUserByEmail(anyString());
        verify(jwtService, never()).generateToken(any(UserDetails.class));
        verify(jwtService, never()).generateRefreshToken(any(UserDetails.class));
        verify(tokenRepository, never()).save(any(Token.class));
    }

    @ParameterizedTest
    @MethodSource("provideRefreshTokenTestData")
    void testRefreshToken_SuccessfulRefresh(String authorizationHeader, String username, String expectedAccessToken) throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(authorizationHeader);
        when(jwtService.extractUsername(authorizationHeader.substring(7))).thenReturn(username);
        when(userService.findUserByEmail(username)).thenReturn(createTestUserResponse());
        when(jwtService.isTokenValid(authorizationHeader.substring(7), createTestUser())).thenReturn(true);
        when(jwtService.generateToken(createTestUser())).thenReturn(expectedAccessToken);
        when(userMapper.fromResponseDTO(any(UserResponseDTO.class))).thenReturn(createTestUser());
        authenticateService.refreshToken(request, response);

        verify(tokenRepository, times(1)).findAllValidTokenByUser(anyLong());

        String expectedJson = String.format("{\"access_token\":\"%s\",\"refresh_token\":\"%s\"}", expectedAccessToken, authorizationHeader.substring(7));
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
        testUser.setId(1L);
        testUser.setEmail(authenticationDTO.getEmail());

        Token validToken1 = Token.builder().expired(false).revoked(false).build();
        Token validToken2 = Token.builder().expired(false).revoked(false).build();

        when(userService.findUserByEmail(authenticationDTO.getEmail())).thenReturn(createTestUserResponse());
        when(userMapper.fromResponseDTO(createTestUserResponse())).thenReturn(createTestUser());
        when(jwtService.generateToken(createTestUser())).thenReturn("mockedAccessToken");
        when(jwtService.generateRefreshToken(createTestUser())).thenReturn("mockedRefreshToken");
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
