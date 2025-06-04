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

import java.lang.reflect.Method;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TokenVerificationServiceTest {

    private TokenVerificationService tokenService;
    private final String rawKey = "super-secret-key-for-jwt-tests-1234567890";
    private final String secretKey = Base64.getEncoder().encodeToString(rawKey.getBytes());

    private String generateToken(
            String userId,
            String email,
            String role,
            String name,
            long ttlMillis
    ) {
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
        String userId = UUID.randomUUID().toString();
        String token = generateToken(
                userId,
                "user@example.com",
                "PACILIAN",
                "John Doe",
                60_000L
        );

        TokenVerificationResponseDto result = tokenService.verifyToken(token);

        assertTrue(result.isValid());
        assertEquals("user@example.com", result.getEmail());
        assertEquals(Role.PACILIAN, result.getRole());
        assertEquals("John Doe", result.getName());
        assertTrue(result.getExpiresIn() > 0 && result.getExpiresIn() <= 60_000L);
    }

    @Test
    void testVerifyToken_shouldThrowExceptionWhenMissingAllThreeClaims() {
        byte[] decodedKey = Base64.getDecoder().decode(secretKey);
        Key key = Keys.hmacShaKeyFor(decodedKey);
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        Date exp = new Date(nowMillis + 60_000L);

        String incompleteToken = Jwts.builder()
                .setSubject("user@example.com")
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        AuthenticationException ex =
                assertThrows(AuthenticationException.class, () -> tokenService.verifyToken(incompleteToken));
        assertEquals("Error verifying token: Invalid token: missing required claims", ex.getMessage());
    }

    @Test
    void testVerifyToken_shouldThrowExceptionWhenRoleIsInvalid() {
        String userId = UUID.randomUUID().toString();
        String badRoleToken = generateToken(userId, "x@y.com", "UNKNOWN", "Alice", 60_000L);

        AuthenticationException ex =
                assertThrows(AuthenticationException.class, () -> tokenService.verifyToken(badRoleToken));
        assertEquals("Error verifying token: Invalid role in token: UNKNOWN", ex.getMessage());
    }

    @Test
    void testVerifyToken_shouldThrowExceptionWhenTokenExpired() throws InterruptedException {
        String userId = UUID.randomUUID().toString();
        String almostImmediateToken = generateToken(userId, "a@b.com", "PACILIAN", "Bob", 1L);
        Thread.sleep(5L);

        AuthenticationException ex =
                assertThrows(AuthenticationException.class, () -> tokenService.verifyToken(almostImmediateToken));
        assertEquals("Error verifying token: Token has expired", ex.getMessage());
    }

    @Test
    void testGetUserIdFromToken_shouldReturnUUID() {
        String userId = UUID.randomUUID().toString();
        String token = generateToken(userId, "u@v.com", "PACILIAN", "Charlie", 60_000L);

        UUID extractedId = tokenService.getUserIdFromToken(token);
        assertEquals(UUID.fromString(userId), extractedId);
    }

    @Test
    void testGetRoleFromToken_shouldThrowWhenRoleInvalid() {
        String userId = UUID.randomUUID().toString();
        String token = generateToken(userId, "role@test.com", "CARE GIVER", "Dana", 60_000L);

        AuthenticationException ex =
                assertThrows(AuthenticationException.class, () -> tokenService.getRoleFromToken(token));
        assertEquals("Error verifying token: Invalid role in token: CARE GIVER", ex.getMessage());
    }

    @Test
    void testValidateRole_shouldThrowExceptionIfRoleMismatch() {
        String userId = UUID.randomUUID().toString();
        String token = generateToken(userId, "m@m.com", "PACILIAN", "Eve", 60_000L);

        AuthenticationException ex =
                assertThrows(AuthenticationException.class, () -> tokenService.validateRole(token, Role.CAREGIVER));
        assertEquals("Access denied. Required role: CAREGIVER", ex.getMessage());
    }

    @Test
    void testValidateRole_shouldNotThrowWhenRoleMatches() {
        String userId = UUID.randomUUID().toString();
        String goodToken = generateToken(userId, "yes@yes.com", "PACILIAN", "Frank", 60_000L);

        assertDoesNotThrow(() -> tokenService.validateRole(goodToken, Role.PACILIAN));
    }

    @Test
    void testIsTokenExpired_directlyViaVerifyToken() {
        String userId = UUID.randomUUID().toString();
        String immediateExpireToken = generateToken(userId, "exp@exp.com", "PACILIAN", "Grace", 1L);
        try {
            Thread.sleep(5L);
        } catch (InterruptedException ignored) {}

        AuthenticationException ex =
                assertThrows(AuthenticationException.class, () -> tokenService.verifyToken(immediateExpireToken));
        assertEquals("Error verifying token: Token has expired", ex.getMessage());
    }

    @Test
    void testPrivateIsTokenExpiredMethod_reflection() throws Exception {
        String userId = UUID.randomUUID().toString();
        String validToken = generateToken(userId, "x@x.com", "PACILIAN", "Henry", 60_000L);
        String expiredToken = generateToken(userId, "y@y.com", "PACILIAN", "Irene", 1L);
        Thread.sleep(5L);

        Method isTokenExpired = TokenVerificationService.class.getDeclaredMethod("isTokenExpired", String.class);
        isTokenExpired.setAccessible(true);

        Boolean validNotExpired = (Boolean) isTokenExpired.invoke(tokenService, validToken);
        Boolean expired = (Boolean) isTokenExpired.invoke(tokenService, expiredToken);

        assertFalse(validNotExpired);
        assertTrue(expired);
    }

    @Test
    void testPrivateGetRemainingTime_reflection() throws Exception {
        String userId = UUID.randomUUID().toString();
        String validToken = generateToken(userId, "rem@rem.com", "PACILIAN", "Jack", 60_000L);
        String expiredToken = generateToken(userId, "done@done.com", "PACILIAN", "Karen", 1L);
        Thread.sleep(5L);

        Method getRemainingTime = TokenVerificationService.class.getDeclaredMethod("getRemainingTime", String.class);
        getRemainingTime.setAccessible(true);

        Long remainingValid = (Long) getRemainingTime.invoke(tokenService, validToken);
        Long remainingExpired = (Long) getRemainingTime.invoke(tokenService, expiredToken);

        assertTrue(remainingValid > 0);
        assertEquals(0L, remainingExpired);
    }

    @Test
    void testVerifyToken_malformedToken() {
        String bad = "not.a.jwt.token";
        AuthenticationException ex =
                assertThrows(AuthenticationException.class, () -> tokenService.verifyToken(bad));
        assertTrue(ex.getMessage().startsWith("Error verifying token:"));
    }

    @Test
    void testVerifyToken_forceGenericExceptionBranch() {
        String userId = UUID.randomUUID().toString();
        String token = generateToken(userId, "sigfail@f.com", "PACILIAN", "Gina", 60_000L);

        String[] parts = token.split("\\.");
        String header = parts[0];
        String payload = parts[1];
        String signature = parts[2];
        String tamperedPayload = payload.substring(0, payload.length() - 1) + "X";
        String brokenToken = header + "." + tamperedPayload + "." + signature;

        AuthenticationException ex = assertThrows(
            AuthenticationException.class,
            () -> tokenService.verifyToken(brokenToken)
        );

        assertTrue(ex.getMessage().startsWith("Error verifying token: "));
    }

}
