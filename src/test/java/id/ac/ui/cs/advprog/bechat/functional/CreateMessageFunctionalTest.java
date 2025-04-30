package id.ac.ui.cs.advprog.bechat.functional;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.bechat.dto.SendMessageRequest;
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

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class CreateMessageFunctionalTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private ChatSession session;
    private UUID senderId;

    @BeforeEach
    void setUp() {
        chatMessageRepository.deleteAll();
        chatSessionRepository.deleteAll();

        senderId = UUID.randomUUID();

        session = new ChatSession();
        session.setId(UUID.randomUUID());
        session.setUser1Id(senderId);
        session.setUser2Id(UUID.randomUUID());
        session.setCreatedAt(LocalDateTime.now());
        session = chatSessionRepository.save(session);
    }

    @Test
    void whenSendMessage_shouldSaveMessageToRepositoryAndReturnIt() throws Exception {
        SendMessageRequest request = new SendMessageRequest();
        request.setSessionId(session.getId());
        request.setSenderId(senderId);
        request.setContent("ini create message");

        mockMvc.perform(post("/api/v1/chat/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("ini create message"))
                .andExpect(jsonPath("$.senderId").value(senderId.toString()))
                .andExpect(jsonPath("$.session.id").value(session.getId().toString()));

        assertThat(chatMessageRepository.findAll()).hasSize(1);
    }
}
