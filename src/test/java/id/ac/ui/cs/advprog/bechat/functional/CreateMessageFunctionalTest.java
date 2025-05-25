package id.ac.ui.cs.advprog.bechat.functional;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.bechat.dto.SendMessageRequest;
import id.ac.ui.cs.advprog.bechat.model.ChatSession;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class CreateMessageFunctionalTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    private ChatSession session;
    private static final String FAKE_TOKEN = "Bearer faketoken";

    @BeforeEach
    void setUp() {
        chatMessageRepository.deleteAll();
        chatSessionRepository.deleteAll();

        session = new ChatSession();
        session.setId(UUID.randomUUID());
        session.setPacilian(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        session.setCaregiver(UUID.fromString("22222222-2222-2222-2222-222222222222"));
        session.setCreatedAt(LocalDateTime.now());
        chatSessionRepository.save(session);
    }

    @Test
    void whenSendMessage_shouldSaveMessageToRepositoryAndReturnIt() throws Exception {
        SendMessageRequest request = new SendMessageRequest();
        request.setSessionId(session.getId());
        request.setContent("di aeon"); 

        mockMvc.perform(post("/api/chat/send")
                        .header("Authorization", FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.senderId").value(session.getPacilian().toString()))
                .andExpect(jsonPath("$.data.content").value("di aeon"))
                .andExpect(jsonPath("$.data.edited").value(false))
                .andExpect(jsonPath("$.data.deleted").value(false));
    }
}
