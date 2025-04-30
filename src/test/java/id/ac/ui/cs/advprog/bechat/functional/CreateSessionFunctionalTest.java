package id.ac.ui.cs.advprog.bechat.functional;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.bechat.dto.CreateSessionRequest;
import id.ac.ui.cs.advprog.bechat.model.builder.ChatSession;
import id.ac.ui.cs.advprog.bechat.repository.ChatMessageRepository;
import id.ac.ui.cs.advprog.bechat.repository.ChatSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class CreateSessionFunctionalTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChatSessionRepository chatSessionRepository;
    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID user1Id;
    private UUID user2Id;

    @BeforeEach
    void setUp() {
        chatMessageRepository.deleteAll();
        chatSessionRepository.deleteAll();
        user1Id = UUID.randomUUID();
        user2Id = UUID.randomUUID();
    }

    @Test
    void createSession_shouldSaveAndReturnSession() throws Exception {
        CreateSessionRequest request = new CreateSessionRequest();
        request.setUser1Id(user1Id);
        request.setUser2Id(user2Id);

        mockMvc.perform(post("/api/v1/chat/session/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user1Id").value(user1Id.toString()))
                .andExpect(jsonPath("$.user2Id").value(user2Id.toString()));

        ChatSession saved = chatSessionRepository.findAll().get(0);
        assertThat(saved.getUser1Id()).isEqualTo(user1Id);
        assertThat(saved.getUser2Id()).isEqualTo(user2Id);
    }
}
