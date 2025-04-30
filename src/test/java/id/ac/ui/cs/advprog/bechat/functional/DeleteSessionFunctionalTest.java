package id.ac.ui.cs.advprog.bechat.functional;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.bechat.model.builder.ChatSession;
import id.ac.ui.cs.advprog.bechat.repository.ChatSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class DeleteSessionFunctionalTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID sessionId;

    @BeforeEach
    void setUp() {
        chatSessionRepository.deleteAll();

        ChatSession session = new ChatSession();
        session.setId(UUID.randomUUID());
        session.setUser1Id(UUID.randomUUID());
        session.setUser2Id(UUID.randomUUID());
        session.setCreatedAt(LocalDateTime.now());

        session = chatSessionRepository.save(session);
        sessionId = session.getId();
    }

    @Test
    void deleteSession_shouldRemoveFromRepository() throws Exception {
        mockMvc.perform(delete("/api/v1/chat/session/" + sessionId))
                .andExpect(status().isNoContent());

        assertThat(chatSessionRepository.findById(sessionId)).isEmpty();
    }
}
