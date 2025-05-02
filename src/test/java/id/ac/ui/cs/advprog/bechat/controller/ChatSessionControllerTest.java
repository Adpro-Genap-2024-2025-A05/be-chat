package id.ac.ui.cs.advprog.bechat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.bechat.dto.CreateSessionRequest;
import id.ac.ui.cs.advprog.bechat.model.ChatSession;
import id.ac.ui.cs.advprog.bechat.service.ChatSessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatSessionController.class)
class ChatSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatSessionService chatSessionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testFindSession_found() throws Exception {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        ChatSession session = new ChatSession();
        session.setId(UUID.randomUUID());
        session.setUser1Id(user1);
        session.setUser2Id(user2);
        session.setCreatedAt(LocalDateTime.now());

        when(chatSessionService.findSession(user1, user2)).thenReturn(Optional.of(session));

        mockMvc.perform(get("/api/v1/chat/session/find")
                        .param("user1", user1.toString())
                        .param("user2", user2.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user1Id").value(user1.toString()))
                .andExpect(jsonPath("$.user2Id").value(user2.toString()));
    }

    @Test
    void testFindSession_notFound() throws Exception {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();

        when(chatSessionService.findSession(user1, user2)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/chat/session/find")
                        .param("user1", user1.toString())
                        .param("user2", user2.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateSession() throws Exception {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        CreateSessionRequest request = new CreateSessionRequest();
        request.setUser1Id(user1);
        request.setUser2Id(user2);

        ChatSession session = new ChatSession();
        session.setId(UUID.randomUUID());
        session.setUser1Id(user1);
        session.setUser2Id(user2);
        session.setCreatedAt(LocalDateTime.now());

        when(chatSessionService.createSession(user1, user2)).thenReturn(session);

        mockMvc.perform(post("/api/v1/chat/session/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user1Id").value(user1.toString()))
                .andExpect(jsonPath("$.user2Id").value(user2.toString()));
    }

    @Test
    void testGetSessionsByUser() throws Exception {
        UUID userId = UUID.randomUUID();
        ChatSession session = new ChatSession();
        session.setId(UUID.randomUUID());
        session.setUser1Id(userId);
        session.setUser2Id(UUID.randomUUID());
        session.setCreatedAt(LocalDateTime.now());

        when(chatSessionService.getSessionsByUser(userId)).thenReturn(List.of(session));

        mockMvc.perform(get("/api/v1/chat/session/user/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].user1Id").value(userId.toString()));
    }

    @Test
    void testDeleteSession() throws Exception {
        UUID sessionId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/chat/session/" + sessionId))
                .andExpect(status().isNoContent());
    }

}
