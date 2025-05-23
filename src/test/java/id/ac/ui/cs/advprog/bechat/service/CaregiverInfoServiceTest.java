package id.ac.ui.cs.advprog.bechat.service;

import id.ac.ui.cs.advprog.bechat.dto.ApiResponseDto;
import id.ac.ui.cs.advprog.bechat.dto.CaregiverPublicDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;

import java.lang.reflect.Field;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CaregiverInfoServiceTest {

    private RestTemplate restTemplate;
    private CaregiverInfoService caregiverInfoService;

    private final String AUTH_URL = "http://localhost:8080";

    @BeforeEach
    void setUp() throws Exception {
        restTemplate = mock(RestTemplate.class);
        caregiverInfoService = new CaregiverInfoService(restTemplate);

        Field authUrlField = CaregiverInfoService.class.getDeclaredField("authServiceUrl");
        authUrlField.setAccessible(true);
        authUrlField.set(caregiverInfoService, AUTH_URL);
    }

    @Test
    void testGetNameByUserIdCaregiver_success() {
        UUID userId = UUID.randomUUID();
        String token = "fake-token";

        CaregiverPublicDto mockCaregiver = new CaregiverPublicDto();
        mockCaregiver.setName("Dr. Cleo");
        ApiResponseDto<CaregiverPublicDto> responseDto = ApiResponseDto.success(200, "OK", mockCaregiver);
        ResponseEntity<ApiResponseDto<CaregiverPublicDto>> responseEntity = new ResponseEntity<>(responseDto, HttpStatus.OK);

        when(restTemplate.exchange(
            eq(AUTH_URL + "/data/caregiver/" + userId),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            ArgumentMatchers.<ParameterizedTypeReference<ApiResponseDto<CaregiverPublicDto>>>any()
    )).thenReturn(responseEntity);

        String result = caregiverInfoService.getNameByUserIdCaregiver(userId, token);

        assertEquals("Dr. Cleo", result);
    }

    @Test
    void testGetNameByUserIdCaregiver_nullUserId_shouldThrow() {
        String token = "fake-token";
        assertThrows(IllegalArgumentException.class, () ->
                caregiverInfoService.getNameByUserIdCaregiver(null, token)
        );
    }

    @Test
    void testGetNameByUserIdCaregiver_nullResponseBody_shouldThrow() {
        UUID userId = UUID.randomUUID();
        String token = "fake-token";

        ResponseEntity<ApiResponseDto<CaregiverPublicDto>> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<ApiResponseDto<CaregiverPublicDto>>>any()
        )).thenReturn(responseEntity);

        assertThrows(NullPointerException.class, () ->
                caregiverInfoService.getNameByUserIdCaregiver(userId, token)
        );
    }

    @Test
    void testGetNameByUserIdCaregiver_bodyWithNullName_shouldThrow() {
        UUID userId = UUID.randomUUID();
        String token = "fake-token";

        CaregiverPublicDto caregiverPublicDto = new CaregiverPublicDto();
        ApiResponseDto<CaregiverPublicDto> responseDto = ApiResponseDto.success(200, "OK", caregiverPublicDto);
        ResponseEntity<ApiResponseDto<CaregiverPublicDto>> responseEntity = new ResponseEntity<>(responseDto, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<ApiResponseDto<CaregiverPublicDto>>>any()
        )).thenReturn(responseEntity);

        assertThrows(IllegalStateException.class, () ->
                caregiverInfoService.getNameByUserIdCaregiver(userId, token)
        );
    }
}
