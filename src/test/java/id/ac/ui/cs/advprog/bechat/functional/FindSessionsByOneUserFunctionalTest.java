package id.ac.ui.cs.advprog.bechat.functional;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.bechat.model.builder.ChatSession;
import id.ac.ui.cs.advprog.bechat.repository.ChatSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
public class FindSessionsByOneUserFunctionalTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID userId;
    private UUID anotherUser;

    @BeforeEach
    void setUp() {
        chatSessionRepository.deleteAll();
        userId = UUID.randomUUID();
        anotherUser = UUID.randomUUID();

        ChatSession session1 = new ChatSession();
        session1.setId(UUID.randomUUID());
        session1.setUser1Id(userId);
        session1.setUser2Id(UUID.randomUUID());
        session1.setCreatedAt(LocalDateTime.now());

        ChatSession session2 = new ChatSession();
        session2.setId(UUID.randomUUID());
        session2.setUser1Id(UUID.randomUUID());
        session2.setUser2Id(userId);
        session2.setCreatedAt(LocalDateTime.now());

        ChatSession other = new ChatSession();
        other.setId(UUID.randomUUID());
        other.setUser1Id(anotherUser);
        other.setUser2Id(UUID.randomUUID());
        other.setCreatedAt(LocalDateTime.now());

        chatSessionRepository.saveAll(List.of(session1, session2, other));
    }

    @Test
    void getSessionsByUser_shouldReturnCorrectSessions() throws Exception {
        mockMvc.perform(get("/api/v1/chat/session/user/" + userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getSessionsByUser_shouldReturnEmptyArrayIfNone() throws Exception {
        UUID unknownUser = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/chat/session/user/" + unknownUser)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
