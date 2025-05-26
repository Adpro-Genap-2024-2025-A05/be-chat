package id.ac.ui.cs.advprog.bechat.service;

import id.ac.ui.cs.advprog.bechat.model.enums.Role;
import id.ac.ui.cs.advprog.bechat.dto.TokenVerificationResponseDto;
import id.ac.ui.cs.advprog.bechat.exception.AuthenticationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Service
public class TokenVerificationService {

    private static final Logger logger = LoggerFactory.getLogger(TokenVerificationService.class);

    @Value("${jwt.secret}")
    private String secretKey;

    public TokenVerificationResponseDto verifyToken(String token) {
        try {
            if (isTokenExpired(token)) {
                logger.warn("Token is expired");
                throw new AuthenticationException("Token has expired");
            }

            Claims claims = extractAllClaims(token);
            String email = extractUsername(token);
            String userId = claims.get("id", String.class);
            String roleStr = claims.get("role", String.class);
            String name = claims.get("name", String.class);

            if (userId == null || roleStr == null || name == null) {
                logger.warn("Missing claims in token: userId={}, role={}, name={}", userId, roleStr, name);
                throw new AuthenticationException("Invalid token: missing required claims");
            }

            if (!Role.contains(roleStr)) {
                logger.warn("Invalid role found in token: {}", roleStr);
                throw new AuthenticationException("Invalid role in token: " + roleStr);
            }

            Role role = Role.valueOf(roleStr);
            long expiresIn = getRemainingTime(token);

            logger.info("Token verified successfully for userId={}, role={}, email={}", userId, role, email);

            return TokenVerificationResponseDto.builder()
                    .valid(true)
                    .userId(userId)
                    .email(email)
                    .role(role)
                    .expiresIn(expiresIn)
                    .name(name)
                    .build();

        } catch (ExpiredJwtException e) {
            logger.warn("Token expired: {}", e.getMessage());
            throw new AuthenticationException("Token has expired");
        } catch (Exception e) {
            logger.error("Token verification failed: {}", e.getMessage(), e);
            throw new AuthenticationException("Error verifying token: " + e.getMessage());
        }
    }

    public UUID getUserIdFromToken(String token) {
        UUID userId = UUID.fromString(verifyToken(token).getUserId());
        return userId;
    }

    public Role getRoleFromToken(String token) {
        Role role = verifyToken(token).getRole();
        return role;
    }

    public void validateRole(String token, Role expectedRole) {
        Role userRole = getRoleFromToken(token);
        if (userRole != expectedRole) {
            logger.warn("Access denied. Expected role: {}, but got: {}", expectedRole, userRole);
            throw new AuthenticationException("Access denied. Required role: " + expectedRole);
        }
        logger.info("Access granted for role {}", userRole);
    }

    private String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) throws ExpiredJwtException {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (ExpiredJwtException e) {
            logger.warn("Token already expired while checking expiration");
            return true;
        }
    }

    private long getRemainingTime(String token) {
        try {
            Date expiration = extractExpiration(token);
            long remainingTime = expiration.getTime() - System.currentTimeMillis();
            return Math.max(0, remainingTime);
        } catch (ExpiredJwtException e) {
            logger.warn("Token already expired while calculating remaining time");
            return 0;
        }
    }
}
