package id.ac.ui.cs.advprog.bechat.service;

import id.ac.ui.cs.advprog.bechat.dto.TokenVerificationResponseDto;
import id.ac.ui.cs.advprog.bechat.exception.AuthenticationException;
import id.ac.ui.cs.advprog.bechat.model.enums.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Base64;

class TokenVerificationServiceTest {

    private TokenVerificationService tokenService;

    private final String rawKey = "super-secret-key-for-jwt-tests-1234567890";
    private final String secretKey = Base64.getEncoder().encodeToString(rawKey.getBytes());

    private String generateToken(String userId, String email, String role, String name, long ttlMillis) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        Date exp = new Date(nowMillis + ttlMillis);

        byte[] decodedKey = Base64.getDecoder().decode(secretKey);
        Key key = Keys.hmacShaKeyFor(decodedKey);

        return Jwts.builder()
                .setSubject(email)
                .claim("id", userId)
                .claim("role", role)
                .claim("name", name)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }


    @BeforeEach
    void setUp() {
        tokenService = new TokenVerificationService();
        ReflectionTestUtils.setField(tokenService, "secretKey", secretKey);

    }

    @Test
    void testVerifyToken_shouldReturnValidResponse() {
        String token = generateToken(UUID.randomUUID().toString(), "user@example.com", "PACILIAN", "John Doe", 60000);
        TokenVerificationResponseDto result = tokenService.verifyToken(token);

        assertTrue(result.isValid());
        assertEquals("user@example.com", result.getEmail());
        assertEquals(Role.PACILIAN, result.getRole());
        assertEquals("John Doe", result.getName());
    }

    @Test
    void testVerifyToken_shouldThrowExceptionWhenMissingClaims() {
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes());
        String token = Jwts.builder()
                .setSubject("user@example.com")
                .setExpiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        assertThrows(AuthenticationException.class, () -> tokenService.verifyToken(token));
    }

    @Test
    void testGetUserIdFromToken_shouldReturnUUID() {
        String userId = UUID.randomUUID().toString();
        String token = generateToken(userId, "user@example.com", "PACILIAN", "John Doe", 60000);
        UUID extractedId = tokenService.getUserIdFromToken(token);
        assertEquals(UUID.fromString(userId), extractedId);
    }

    @Test
    void testGetRoleFromToken_shouldReturnCorrectRole() {
        String token = generateToken(UUID.randomUUID().toString(), "user@example.com", "PACILIAN", "John Doe", 60000);
        Role role = tokenService.getRoleFromToken(token);
        assertEquals(Role.PACILIAN, role);
    }

    @Test
    void testValidateRole_shouldThrowExceptionIfRoleMismatch() {
        String token = generateToken(UUID.randomUUID().toString(), "user@example.com", "PACILIAN", "John Doe", 60000);
        assertThrows(AuthenticationException.class, () -> tokenService.validateRole(token, Role.CAREGIVER));
    }
}
