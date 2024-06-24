package org.example.roomrelish.services.authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.example.roomrelish.exception.CustomerAlreadyExistsException;
import org.example.roomrelish.dto.AuthResponse;
import org.example.roomrelish.dto.LoginRequest;
import org.example.roomrelish.dto.RegisterRequest;
import org.example.roomrelish.models.Customer;
import org.example.roomrelish.models.Role;
import org.example.roomrelish.models.Token;
import org.example.roomrelish.repository.CustomerRepository;
import org.example.roomrelish.repository.TokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;


@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private Customer customer;
    private final String encodedPassword = "encodedPassword";
    private final String jwtToken = "jwtToken";

    @BeforeEach
    public void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("username");
        registerRequest.setPassword("password");
        registerRequest.setEmail("email@example.com");
        registerRequest.setPhoneNumber("1234567890");
        registerRequest.setAddress("Address");
        registerRequest.setDateOfBirth(new Date());

        loginRequest = new LoginRequest();
        loginRequest.setEmail("email@example.com");
        loginRequest.setPassword("password");

        customer = new Customer();
        customer.setUsername(registerRequest.getUsername());
        customer.setPassword(encodedPassword);
        customer.setEmail(registerRequest.getEmail());
        customer.setPhoneNumber(registerRequest.getPhoneNumber());
        customer.setAddress(registerRequest.getAddress());
        customer.setRegisteredAt(new Date());
        customer.setDateOfBirth(registerRequest.getDateOfBirth());
        customer.setRole(Role.USER);
    }

    @Test
     void testRegisterCustomer_Success() {
        when(customerRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        when(customerRepository.findByPhoneNumber(registerRequest.getPhoneNumber())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn(encodedPassword);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(jwtService.generateToken(customer)).thenReturn(jwtToken);

        AuthResponse authResponse = authService.registerCustomer(registerRequest);

        assertNotNull(authResponse);
        assertEquals(jwtToken, authResponse.getToken());
        assertEquals(customer.getId(), authResponse.getUserId());
        verify(customerRepository).save(any(Customer.class));
        verify(tokenRepository).save(any(Token.class));
    }

    @Test
     void testRegisterCustomer_EmailAlreadyExists() {
        when(customerRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.of(customer));

        assertThrows(CustomerAlreadyExistsException.class, () -> authService.registerCustomer(registerRequest));

        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
     void testAuthenticate_Success() {
        when(customerRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(customer));
        when(jwtService.generateToken(customer)).thenReturn(jwtToken);

        AuthResponse authResponse = authService.authenticate(loginRequest);

        assertNotNull(authResponse);
        assertEquals(jwtToken, authResponse.getToken());
        assertEquals(customer.getId(), authResponse.getUserId());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenRepository).save(any(Token.class));
    }

    @Test
     void testAuthenticate_UserNotFound() {
        when(customerRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> authService.authenticate(loginRequest));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenRepository, never()).save(any(Token.class));
    }

    @Test
     void testRevokeAllUserTokens() {
        List<Token> validTokens = new ArrayList<>();
        Token token1 = new Token();
        token1.setExpired(false);
        token1.setRevoked(false);
        validTokens.add(token1);

        Token token2 = new Token();
        token2.setExpired(false);
        token2.setRevoked(false);
        validTokens.add(token2);

        when(tokenRepository.findAllByCustomerIdAndExpiredIsFalseAndRevokedIsFalse(customer.getId())).thenReturn(validTokens);

        authService.revokeAllUserTokens(customer);

        assertTrue(validTokens.get(0).isExpired());
        assertTrue(validTokens.get(0).isRevoked());
        assertTrue(validTokens.get(1).isExpired());
        assertTrue(validTokens.get(1).isRevoked());
        verify(tokenRepository).saveAll(validTokens);
    }

    @Test
     void testRevokeAllUserTokens_NoValidTokens() {
        when(tokenRepository.findAllByCustomerIdAndExpiredIsFalseAndRevokedIsFalse(customer.getId())).thenReturn(Collections.emptyList());

        authService.revokeAllUserTokens(customer);

        verify(tokenRepository, never()).saveAll(anyList());
    }
}
