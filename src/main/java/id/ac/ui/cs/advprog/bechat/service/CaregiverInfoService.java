package id.ac.ui.cs.advprog.bechat.service;

import id.ac.ui.cs.advprog.bechat.dto.UserInfoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CaregiverInfoService {

    private final RestTemplate restTemplate;

    @Value("${auth.service.url}")
    private String authServiceUrl;

    public String getNameByUserIdCaregiver(UUID userId, String token) {
        if (userId == null) {
            throw new IllegalArgumentException("Caregiver userId is null");
        }
    
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<UserInfoDTO> response = restTemplate.exchange(
                authServiceUrl + "/auth/caregiver/" + userId,
                HttpMethod.GET,
                entity,
                UserInfoDTO.class
        );

        UserInfoDTO body = response.getBody();
        if (body == null || body.getName() == null) {
            throw new IllegalStateException("Failed to retrieve caregiver name from auth service");
        }

        return Objects.requireNonNull(response.getBody()).getName();

    }
}