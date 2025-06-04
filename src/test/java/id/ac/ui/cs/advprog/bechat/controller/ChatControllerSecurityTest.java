package id.ac.ui.cs.advprog.bechat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.bechat.config.SecurityConfig;
import id.ac.ui.cs.advprog.bechat.dto.TokenVerificationResponseDto;
import id.ac.ui.cs.advprog.bechat.model.ChatSession;
import id.ac.ui.cs.advprog.bechat.model.enums.Role;
import id.ac.ui.cs.advprog.bechat.service.CaregiverInfoService;
import id.ac.ui.cs.advprog.bechat.service.ChatSessionService;
import id.ac.ui.cs.advprog.bechat.service.TokenVerificationService;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ChatSessionController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc(addFilters = true)
public class ChatControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatSessionService chatSessionService;

    @MockBean
    private TokenVerificationService tokenVerificationService;

    @MockBean
    private CaregiverInfoService caregiverInfoService;

    @MockBean
    private MeterRegistry meterRegistry;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String VALID_TOKEN   = "validtoken";
    private static final String INVALID_TOKEN = "invalid";

    @Test
    void whenNoAuthorizationHeader_thenForbidden() throws Exception {
        String payload = "{\"caregiver\":\"" + UUID.randomUUID() + "\"}";

        mockMvc.perform(post("/api/chat/session/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    void whenAuthorizationHeaderInvalidPrefix_thenForbidden() throws Exception {
        String payload = "{\"caregiver\":\"" + UUID.randomUUID() + "\"}";

        mockMvc.perform(post("/api/chat/session/create")
                        .header("Authorization", INVALID_TOKEN + " abc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    void whenTokenNotVerified_thenForbidden() throws Exception {
        String payload = "{\"caregiver\":\"" + UUID.randomUUID() + "\"}";

        Mockito.when(tokenVerificationService.verifyToken(INVALID_TOKEN))
               .thenThrow(new RuntimeException("Invalid token"));

        mockMvc.perform(post("/api/chat/session/create")
                        .header("Authorization", "Bearer " + INVALID_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    void whenTokenVerifiedButRoleNotPacilian_thenForbidden() throws Exception {
        String payload = "{\"caregiver\":\"" + UUID.randomUUID() + "\"}";

        Mockito.when(tokenVerificationService.verifyToken(VALID_TOKEN))
               .thenReturn(TokenVerificationResponseDto.builder()
                       .userId(UUID.randomUUID().toString())
                       .valid(true)
                       .role(Role.CAREGIVER)
                       .email("care@example.com")
                       .expiresIn(3600L)
                       .build()
               );

        mockMvc.perform(post("/api/chat/session/create")
                        .header("Authorization", "Bearer " + VALID_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    void whenTokenVerifiedAndRolePacilian_thenProceedToController() throws Exception {
        UUID caregiverId = UUID.randomUUID();
        String payload = "{\"caregiver\":\"" + caregiverId + "\"}";
        UUID pacilianId = UUID.randomUUID();

        Mockito.when(tokenVerificationService.verifyToken(VALID_TOKEN))
               .thenReturn(TokenVerificationResponseDto.builder()
                       .userId(pacilianId.toString())
                       .valid(true)
                       .role(Role.PACILIAN)
                       .email("paci@example.com")
                       .expiresIn(3600L)
                       .name("PacilianName")
                       .build()
               );

        ChatSession dummySession = new ChatSession();
        dummySession.setId(UUID.randomUUID());
        dummySession.setPacilian(pacilianId);
        dummySession.setCaregiver(caregiverId);
        dummySession.setCreatedAt(LocalDateTime.now());

        Mockito.when(chatSessionService.createSession(
                        Mockito.eq(pacilianId),
                        Mockito.eq(caregiverId),
                        Mockito.eq(VALID_TOKEN))
               ).thenReturn(dummySession);

        Mockito.when(caregiverInfoService.getNameByUserIdCaregiver(caregiverId, VALID_TOKEN))
               .thenReturn("Dr. Panda");

        mockMvc.perform(post("/api/chat/session/create")
                        .header("Authorization", "Bearer " + VALID_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());
    }
}