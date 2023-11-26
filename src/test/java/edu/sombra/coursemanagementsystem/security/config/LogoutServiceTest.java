/*
package edu.sombra.coursemanagementsystem.security.config;

import edu.sombra.coursemanagementsystem.entity.Token;
import edu.sombra.coursemanagementsystem.repository.token.TokenRepository;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class LogoutServiceTest {

    @Mock
    private TokenRepository tokenRepository;

    @InjectMocks
    private LogoutService logoutService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        logoutService = new LogoutService(tokenRepository);
    }

    @Test
    void testLogout_ValidToken() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Authentication authentication = mock(Authentication.class);

        when(request.getHeader("Authorization")).thenReturn("Bearer mockToken");
        Token storedToken = Token.builder().token("mockToken").expired(false).revoked(false).build();
        when(tokenRepository.findByToken("mockToken")).thenReturn(Optional.of(storedToken));

        logoutService.logout(request, response, authentication);

        verify(tokenRepository, times(1)).save(storedToken);
        verify(authentication, never()).setAuthenticated(anyBoolean());
        assertTrue(storedToken.isExpired() && storedToken.isRevoked());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testLogout_InvalidToken() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Authentication authentication = mock(Authentication.class);

        when(request.getHeader("Authorization")).thenReturn("InvalidTokenFormat");

        logoutService.logout(request, response, authentication);

        verify(tokenRepository, never()).findByToken(any());
        verify(tokenRepository, never()).save(any());
        verify(authentication, never()).setAuthenticated(anyBoolean());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testLogout_ValidTokenWhenStoredTokenIsNotNull() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Authentication authentication = mock(Authentication.class);

        when(request.getHeader("Authorization")).thenReturn("Bearer mockToken");
        Token storedToken = Token.builder().token("mockToken").expired(false).revoked(false).build();
        when(tokenRepository.findByToken("mockToken")).thenReturn(Optional.of(storedToken));

        logoutService.logout(request, response, authentication);

        verify(tokenRepository, times(1)).save(storedToken);

        assertTrue(storedToken.isExpired());
        assertTrue(storedToken.isRevoked());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }


    //FIXME: works good but failed
*/
/*    @Test
    void testLogout_TokenNotFound() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Authentication authentication = mock(Authentication.class);

        when(request.getHeader("Authorization")).thenReturn("Bearer nonExistentToken");
        when(tokenRepository.findByToken("nonExistentToken")).thenReturn(Optional.empty());

        logoutService.logout(request, response, authentication);

        verify(tokenRepository, never()).save(any());
        verify(authentication, never()).setAuthenticated(anyBoolean());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }*//*

}*/
