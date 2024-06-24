package org.example.roomrelish.services.authentication;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
 class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET_KEY = "8d4fed75477d160c393db8a22edce23a5ae7971b4533077d89ac0016dd92c879d21791073310294924cb896443a8214cfdc129baa42af8b3030a397382a93532";
    private static final long JWT_EXPIRATION = 86400000; // 1 day in milliseconds
    private static final long REFRESH_TOKEN_EXPIRATION = 604800000; // 7 days in milliseconds

    @BeforeEach
    public void setUp() throws Exception {
        jwtService = new JwtService();

        // Using reflection to set private fields
        setField(jwtService, "SECRET_KEY", SECRET_KEY);
        setField(jwtService, "jwtExpirationInMs", JWT_EXPIRATION);
        setField(jwtService, "refreshTokenExpirationInMs", REFRESH_TOKEN_EXPIRATION);
    }

    private void setField(Object targetObject, String fieldName, Object value) throws Exception {
        Field field = targetObject.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(targetObject, value);
    }

    @Test
     void testGenerateToken() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("testUser");

        String token = jwtService.generateToken(userDetails);
        assertNotNull(token);

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals("testUser", claims.getSubject());
    }

    @Test
     void testIsTokenValid() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("testUser");

        String token = jwtService.generateToken(userDetails);

        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
     void testIsTokenExpired() {
        Map<String, Object> claims = new HashMap<>();
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("testUser");

        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis() - 10000)) // issued in the past
                .setExpiration(new Date(System.currentTimeMillis() - 5000)) // expired
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()), SignatureAlgorithm.HS256)
                .compact();

        try {
            boolean isExpired = jwtService.isTokenExpired(token);
            assertTrue(isExpired);
        } catch (ExpiredJwtException e) {
            // Expected exception for an expired token
            assertTrue(true);
        }
    }
}
