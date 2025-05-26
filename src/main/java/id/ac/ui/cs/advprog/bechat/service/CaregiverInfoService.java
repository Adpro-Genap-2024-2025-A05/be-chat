package id.ac.ui.cs.advprog.bechat.service;

import id.ac.ui.cs.advprog.bechat.dto.ApiResponseDto;
import id.ac.ui.cs.advprog.bechat.dto.CaregiverPublicDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CaregiverInfoService {

    private static final Logger logger = LoggerFactory.getLogger(CaregiverInfoService.class);

    private final RestTemplate restTemplate;

    @Value("${auth.service.url}")
    private String authServiceUrl;

    public String getNameByUserIdCaregiver(UUID userId, String token) {
        logger.info("Requesting caregiver name for userId: {}", userId);

        if (userId == null) {
            logger.warn("Caregiver userId is null");
            throw new IllegalArgumentException("Caregiver userId is null");
        }

        logger.info("Token length: {}", token.length());
        logger.info("Token starts with: {}", token.substring(0, Math.min(20, token.length())));
        logger.info("Token period count: {}", token.chars().filter(ch -> ch == '.').count());
        
        logger.debug("Full token: {}", token);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            String url = authServiceUrl + "/data/caregiver/" + userId;
            logger.debug("Calling auth service at URL: {}", url);

            ResponseEntity<ApiResponseDto<CaregiverPublicDto>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {}
            );

            if (response.getStatusCode() != HttpStatus.OK) {
                logger.warn("Auth service returned non-OK status: {}", response.getStatusCode());
                throw new IllegalStateException("Failed to retrieve caregiver info: " + response.getStatusCode());
            }

            CaregiverPublicDto caregiver = response.getBody() != null ? response.getBody().getData() : null;

            if (caregiver == null || caregiver.getName() == null) {
                logger.warn("Caregiver data or name is null in response body");
                throw new IllegalStateException("Failed to retrieve caregiver name from auth service");
            }

            logger.info("Successfully retrieved caregiver name: {}", caregiver.getName());
            return caregiver.getName();

        } catch (RestClientException e) {
            logger.error("Error while calling auth service to get caregiver name: {}", e.getMessage(), e);
            throw new IllegalStateException("Error fetching caregiver info from auth service", e);
        }
    }
}
