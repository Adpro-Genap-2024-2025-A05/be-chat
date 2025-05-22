package id.ac.ui.cs.advprog.bechat.service;

import id.ac.ui.cs.advprog.bechat.dto.UserInfoDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.util.Arrays;

class CaregiverInfoServiceTest {

    private RestTemplate restTemplate;
    private CaregiverInfoService caregiverInfoService;

    private final String AUTH_URL = "http://localhost:8080"; 

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        caregiverInfoService = new CaregiverInfoService(restTemplate);

        Arrays.stream(caregiverInfoService.getClass().getDeclaredFields())
                .filter(field -> field.getName().equals("authServiceUrl"))
                .findFirst()
                .ifPresent(field -> {
                    field.setAccessible(true);
                    try {
                        field.set(caregiverInfoService, AUTH_URL);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Test
    void testGetNameByUserIdCaregiver_success() {
        UUID userId = UUID.randomUUID();
        String token = "fake-token";

        UserInfoDTO mockUser = new UserInfoDTO();
        mockUser.setName("Dr. Cleo");

        ResponseEntity<UserInfoDTO> responseEntity = new ResponseEntity<>(mockUser, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(AUTH_URL + "/auth/caregiver/" + userId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(UserInfoDTO.class)
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

        ResponseEntity<UserInfoDTO> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(UserInfoDTO.class)
        )).thenReturn(responseEntity);

        assertThrows(IllegalStateException.class, () ->
                caregiverInfoService.getNameByUserIdCaregiver(userId, token)
        );
    }

    @Test
    void testGetNameByUserIdCaregiver_bodyWithNullName_shouldThrow() {
        UUID userId = UUID.randomUUID();
        String token = "fake-token";

        UserInfoDTO userInfoDTO = new UserInfoDTO(); // name default-nya null
        ResponseEntity<UserInfoDTO> responseEntity = new ResponseEntity<>(userInfoDTO, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(UserInfoDTO.class)
        )).thenReturn(responseEntity);

        assertThrows(IllegalStateException.class, () ->
                caregiverInfoService.getNameByUserIdCaregiver(userId, token)
        );
    }
}
