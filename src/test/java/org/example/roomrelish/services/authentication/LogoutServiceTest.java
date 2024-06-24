package org.example.roomrelish.services.authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.roomrelish.models.Token;
import org.example.roomrelish.repository.TokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;


import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class LogoutServiceTest {

    @Mock
    private TokenRepository tokenRepository;

    @InjectMocks
    private LogoutService logoutService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
     void testLogoutWithoutAuthHeader() {
        when(request.getHeader("Authorization")).thenReturn(null);

        logoutService.logout(request, response, authentication);

        verify(tokenRepository, never()).findByToken(anyString());
    }

    @Test
     void testLogoutWithInvalidAuthHeader() {
        when(request.getHeader("Authorization")).thenReturn("InvalidToken");

        logoutService.logout(request, response, authentication);

        verify(tokenRepository, never()).findByToken(anyString());
    }

    @Test
     void testLogoutWithValidAuthHeaderAndTokenNotFound() {
        when(request.getHeader("Authorization")).thenReturn("Bearer validToken");
        when(tokenRepository.findByToken("validToken")).thenReturn(Optional.empty());

        logoutService.logout(request, response, authentication);

        verify(tokenRepository, times(1)).findByToken("validToken");
        verify(tokenRepository, never()).save(any());
    }

    @Test
     void testLogoutWithValidAuthHeaderAndTokenFound() {
        Token storedToken = new Token();
        when(request.getHeader("Authorization")).thenReturn("Bearer validToken");
        when(tokenRepository.findByToken("validToken")).thenReturn(Optional.of(storedToken));

        logoutService.logout(request, response, authentication);

        verify(tokenRepository, times(1)).findByToken("validToken");
        assertTrue(storedToken.isExpired());
        assertTrue(storedToken.isRevoked());
        verify(tokenRepository, times(1)).save(storedToken);
    }
}
