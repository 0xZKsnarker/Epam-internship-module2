package com.epam.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private Jwtutil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new Jwtutil();

        ReflectionTestUtils.setField(jwtUtil, "secret", "your-super-secret-key-that-is-long-and-secure-and-at-least-256-bits");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 3600000L); // 1 hour
    }

    @Test
    void generateToken_shouldReturnValidToken() {
        UserDetails userDetails = new User("testuser", "password", new ArrayList<>());
        String token = jwtUtil.generateToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void extractUsername_shouldReturnCorrectUsername() {
        UserDetails userDetails = new User("testuser", "password", new ArrayList<>());
        String token = jwtUtil.generateToken(userDetails);

        String extractedUsername = jwtUtil.extractUsername(token);
        assertEquals("testuser", extractedUsername);
    }

    @Test
    void validateToken_withValidToken_shouldReturnTrue() {
        UserDetails userDetails = new User("testuser", "password", new ArrayList<>());
        String token = jwtUtil.generateToken(userDetails);

        assertTrue(jwtUtil.validateToken(token, userDetails));
    }

    @Test
    void validateToken_withInvalidUsername_shouldReturnFalse() {
        UserDetails userDetails = new User("testuser", "password", new ArrayList<>());
        UserDetails otherUserDetails = new User("otheruser", "password", new ArrayList<>());
        String token = jwtUtil.generateToken(userDetails);

        assertFalse(jwtUtil.validateToken(token, otherUserDetails));
    }

    @Test
    void isTokenExpired_withFreshToken_shouldReturnFalse() {
        UserDetails userDetails = new User("testuser", "password", new ArrayList<>());
        String token = jwtUtil.generateToken(userDetails);

        Date expiration = jwtUtil.extractExpiration(token);
        assertFalse(expiration.before(new Date()));
    }
}