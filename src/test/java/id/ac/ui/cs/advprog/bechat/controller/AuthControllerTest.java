package id.ac.ui.cs.advprog.bechat.controller;

import id.ac.ui.cs.advprog.bechat.dto.TokenVerificationResponseDto;
import id.ac.ui.cs.advprog.bechat.model.enums.Role;
import id.ac.ui.cs.advprog.bechat.service.TokenVerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TokenVerificationService tokenVerificationService;

    private static final String VALID_TOKEN = "Bearer faketoken";

    private TokenVerificationResponseDto validResponse;

    @BeforeEach
    void setUp() {
        validResponse = TokenVerificationResponseDto.builder()
                .valid(true)
                .userId(UUID.randomUUID().toString())
                .email("test@example.com")
                .role(Role.PACILIAN)
                .name("Test User")
                .expiresIn(3600L)
                .build();
    }

    @Test
    void testVerifyToken_shouldReturn200WhenTokenValid() throws Exception {
        Mockito.when(tokenVerificationService.verifyToken("faketoken")).thenReturn(validResponse);

        mockMvc.perform(post("/auth/verify")
                        .header(HttpHeaders.AUTHORIZATION, VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Token verified successfully"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }

    @Test
    void testVerifyToken_shouldReturn401WhenNoToken() throws Exception {
        mockMvc.perform(post("/auth/verify"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid authentication token"));
    }

    @Test
    void testVerifyToken_shouldReturn401WhenInvalidToken() throws Exception {
        TokenVerificationResponseDto invalidResponse = TokenVerificationResponseDto.builder()
                .valid(false)
                .build();

        Mockito.when(tokenVerificationService.verifyToken("faketoken")).thenReturn(invalidResponse);

        mockMvc.perform(post("/auth/verify")
                        .header(HttpHeaders.AUTHORIZATION, VALID_TOKEN))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid or expired token"));
    }
}
