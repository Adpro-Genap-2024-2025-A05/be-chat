package id.ac.ui.cs.advprog.bechat.service;

import id.ac.ui.cs.advprog.bechat.dto.ApiResponseDto;
import id.ac.ui.cs.advprog.bechat.dto.CaregiverPublicDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
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
        String token = "header.payload.signature";

        CaregiverPublicDto mockCaregiver = new CaregiverPublicDto();
        mockCaregiver.setName("Dr. Cleo");
        ApiResponseDto<CaregiverPublicDto> responseDto = ApiResponseDto.success(200, "OK", mockCaregiver);
        ResponseEntity<ApiResponseDto<CaregiverPublicDto>> responseEntity =
                new ResponseEntity<>(responseDto, HttpStatus.OK);

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
        String token = "some.token.value";
        assertThrows(IllegalArgumentException.class, () ->
                caregiverInfoService.getNameByUserIdCaregiver(null, token)
        );
    }

    @Test
    void testGetNameByUserIdCaregiver_nullResponseBody_shouldThrow() {
        UUID userId = UUID.randomUUID();
        String token = "abc.def.ghi";

        ResponseEntity<ApiResponseDto<CaregiverPublicDto>> responseEntity =
                new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<ApiResponseDto<CaregiverPublicDto>>>any()
        )).thenReturn(responseEntity);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                caregiverInfoService.getNameByUserIdCaregiver(userId, token)
        );
        assertTrue(ex.getMessage().contains("Failed to retrieve caregiver name"));
    }

    @Test
    void testGetNameByUserIdCaregiver_bodyWithNullName_shouldThrow() {
        UUID userId = UUID.randomUUID();
        String token = "abc.def.ghi";

        CaregiverPublicDto caregiverPublicDto = new CaregiverPublicDto();
        ApiResponseDto<CaregiverPublicDto> responseDto = ApiResponseDto.success(200, "OK", caregiverPublicDto);
        ResponseEntity<ApiResponseDto<CaregiverPublicDto>> responseEntity =
                new ResponseEntity<>(responseDto, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<ApiResponseDto<CaregiverPublicDto>>>any()
        )).thenReturn(responseEntity);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                caregiverInfoService.getNameByUserIdCaregiver(userId, token)
        );
        assertTrue(ex.getMessage().contains("Failed to retrieve caregiver name"));
    }

    @Test
    void testGetNameByUserIdCaregiver_nonOkStatus_shouldThrow() {
        UUID userId = UUID.randomUUID();
        String token = "xyz.abc.def";

        CaregiverPublicDto mockCaregiver = new CaregiverPublicDto();
        mockCaregiver.setName("Dr. Error");
        ApiResponseDto<CaregiverPublicDto> responseDto = ApiResponseDto.success(200, "OK", mockCaregiver);
        ResponseEntity<ApiResponseDto<CaregiverPublicDto>> responseEntity =
                new ResponseEntity<>(responseDto, HttpStatus.NOT_FOUND);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<ApiResponseDto<CaregiverPublicDto>>>any()
        )).thenReturn(responseEntity);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                caregiverInfoService.getNameByUserIdCaregiver(userId, token)
        );
        assertTrue(ex.getMessage().contains("Failed to retrieve caregiver info:"));
    }

    @Test
    void testGetNameByUserIdCaregiver_restClientException_shouldThrow() {
        UUID userId = UUID.randomUUID();
        String token = "part1.part2.part3";

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<ApiResponseDto<CaregiverPublicDto>>>any()
        )).thenThrow(new RestClientException("Connection failed"));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                caregiverInfoService.getNameByUserIdCaregiver(userId, token)
        );
        assertTrue(ex.getMessage().contains("Error fetching caregiver info"));
    }
}
