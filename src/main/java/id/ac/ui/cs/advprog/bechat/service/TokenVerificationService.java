package id.ac.ui.cs.advprog.bechat.service;

import id.ac.ui.cs.advprog.bechat.dto.TokenVerificationResponseDto;
import id.ac.ui.cs.advprog.bechat.exception.AuthenticationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenVerificationService {

    private final RestTemplate restTemplate;

    @Value("${auth.service.url}")
    private String authServiceUrl;

    public TokenVerificationResponseDto verifyToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<TokenVerificationResponseDto> response = restTemplate.exchange(
                    authServiceUrl + "/verify",
                    HttpMethod.POST,
                    entity,
                    TokenVerificationResponseDto.class
            );

            TokenVerificationResponseDto result = response.getBody();
            if (result == null || !result.isValid()) {
                throw new AuthenticationException("Invalid or expired token");
            }

            return result;
        } catch (Exception e) {
            throw new AuthenticationException("Token verification failed: " + e.getMessage());
        }
    }

    public UUID getUserIdFromToken(String token) {
        return UUID.fromString(verifyToken(token).getUserId());
    }
}
