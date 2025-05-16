package id.ac.ui.cs.advprog.bechat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.bechat.dto.CreateSessionRequest;
import id.ac.ui.cs.advprog.bechat.dto.TokenVerificationResponseDto;
import id.ac.ui.cs.advprog.bechat.model.ChatSession;
import id.ac.ui.cs.advprog.bechat.model.enums.Role;
import id.ac.ui.cs.advprog.bechat.service.ChatSessionService;
import id.ac.ui.cs.advprog.bechat.service.TokenVerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatSessionController.class)
@AutoConfigureMockMvc(addFilters = false)
class ChatSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatSessionService chatSessionService;

    @MockBean
    private TokenVerificationService tokenVerificationService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID dummyUserId;
    private static final String DUMMY_TOKEN = "Bearer faketoken";
    private ChatSession dummySession;

    @BeforeEach
    void setUp() {
        dummyUserId = UUID.randomUUID();

        dummySession = new ChatSession();
        dummySession.setId(UUID.randomUUID());
        dummySession.setPacilian(dummyUserId);
        dummySession.setCaregiver(UUID.randomUUID());
        dummySession.setCreatedAt(LocalDateTime.now());

        Mockito.when(tokenVerificationService.verifyToken("faketoken"))
                .thenReturn(TokenVerificationResponseDto.builder()
                        .valid(true)
                        .userId(dummyUserId.toString())
                        .role(Role.PACILIAN)
                        .email("user@example.com")
                        .expiresIn(3600L)
                        .build());
    }

    @Test
    void testCreateSession() throws Exception {
        CreateSessionRequest request = new CreateSessionRequest();
        request.setCaregiver(dummySession.getCaregiver());

        // Tambahkan argumen ketiga: token
        Mockito.when(chatSessionService.createSession(eq(dummyUserId), eq(dummySession.getCaregiver()), anyString()))
                .thenReturn(dummySession);

        mockMvc.perform(post("/chat/session/create")
                        .header("Authorization", DUMMY_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.pacilian").value(dummyUserId.toString()))
                .andExpect(jsonPath("$.data.caregiver").value(dummySession.getCaregiver().toString()));
    }

    @Test
    void testGetSessionsByUser() throws Exception {
        Mockito.when(chatSessionService.getSessionsByUser(eq(dummyUserId)))
                .thenReturn(List.of(dummySession));

        mockMvc.perform(get("/chat/session/user")
                        .header("Authorization", DUMMY_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].pacilian").value(dummyUserId.toString()));
    }
}
