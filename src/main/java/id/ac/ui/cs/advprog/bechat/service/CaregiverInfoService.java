package id.ac.ui.cs.advprog.bechat.service;

import id.ac.ui.cs.advprog.bechat.dto.ApiResponseDto;
import id.ac.ui.cs.advprog.bechat.dto.CaregiverPublicDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;
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

        String url = authServiceUrl + "/data/caregiver/" + userId;

        ResponseEntity<ApiResponseDto<CaregiverPublicDto>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        CaregiverPublicDto caregiver = response.getBody().getData();

        if (caregiver == null || caregiver.getName() == null) {
            throw new IllegalStateException("Failed to retrieve caregiver name from auth service");
        }

        return caregiver.getName();
    }
}