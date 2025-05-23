package id.ac.ui.cs.advprog.bechat.functional;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.bechat.model.ChatSession;
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
    private static final String DUMMY_TOKEN = "Bearer faketoken";

    @BeforeEach
    void setUp() {
        chatSessionRepository.deleteAll();
        userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        anotherUser = UUID.fromString("33333333-3333-3333-3333-333333333333");

        ChatSession session1 = new ChatSession();
        session1.setId(UUID.randomUUID());
        session1.setPacilian(userId);
        session1.setCaregiver(UUID.randomUUID());
        session1.setCreatedAt(LocalDateTime.now());

        ChatSession session2 = new ChatSession();
        session2.setId(UUID.randomUUID());
        session2.setPacilian(UUID.randomUUID());
        session2.setCaregiver(userId);
        session2.setCreatedAt(LocalDateTime.now());

        ChatSession other = new ChatSession();
        other.setId(UUID.randomUUID());
        other.setPacilian(anotherUser);
        other.setCaregiver(UUID.randomUUID());
        other.setCreatedAt(LocalDateTime.now());

        chatSessionRepository.saveAll(List.of(session1, session2, other));
    }

    @Test
    void getSessionsByUser_shouldReturnCorrectSessions() throws Exception {
        mockMvc.perform(get("/api/chat/session/user")
                        .header("Authorization", DUMMY_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void getSessionsByUser_shouldReturnEmptyArrayIfNone() throws Exception {
        mockMvc.perform(get("/api/chat/session/user")
                        .header("Authorization", "Bearer token-for-unknown-user")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }
}
