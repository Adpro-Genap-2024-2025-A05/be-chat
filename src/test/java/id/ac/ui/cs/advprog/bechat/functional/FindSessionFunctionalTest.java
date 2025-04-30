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
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
public class FindSessionFunctionalTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID user1Id;
    private UUID user2Id;
    private ChatSession session;

    @BeforeEach
    void setUp() {
        chatSessionRepository.deleteAll();
        user1Id = UUID.randomUUID();
        user2Id = UUID.randomUUID();

        session = new ChatSession();
        session.setId(UUID.randomUUID());
        session.setUser1Id(user1Id);
        session.setUser2Id(user2Id);
        session.setCreatedAt(LocalDateTime.now());

        chatSessionRepository.save(session);
    }

    @Test
    void findSession_shouldReturnSessionIfExists() throws Exception {
        mockMvc.perform(get("/api/v1/chat/session/find")
                        .param("user1", user1Id.toString())
                        .param("user2", user2Id.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user1Id").value(user1Id.toString()))
                .andExpect(jsonPath("$.user2Id").value(user2Id.toString()));
    }

    @Test
    void findSession_shouldReturnNotFoundIfSessionDoesNotExist() throws Exception {
        UUID otherUser1 = UUID.randomUUID();
        UUID otherUser2 = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/chat/session/find")
                        .param("user1", otherUser1.toString())
                        .param("user2", otherUser2.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
