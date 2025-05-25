package id.ac.ui.cs.advprog.bechat.functional;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.bechat.dto.CreateSessionRequest;
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

    private UUID pacilian;
    private UUID caregiver;

    private static final String FAKE_TOKEN = "Bearer faketoken";

    @BeforeEach
    void setUp() {
        chatMessageRepository.deleteAll();
        chatSessionRepository.deleteAll();
        pacilian = UUID.fromString("11111111-1111-1111-1111-111111111111"); // dari token
        caregiver = UUID.fromString("22222222-2222-2222-2222-222222222222");
    }

    @Test
    void createSession_shouldSaveAndReturnSession() throws Exception {
        CreateSessionRequest request = new CreateSessionRequest();
        request.setCaregiver(caregiver); 

        mockMvc.perform(post("/api/chat/session/create")
                        .header("Authorization", FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pacilian").value(pacilian.toString()))
                .andExpect(jsonPath("$.data.caregiver").value(caregiver.toString()));

        ChatSession saved = chatSessionRepository.findAll().get(0);
        assertThat(saved.getPacilian()).isEqualTo(pacilian);
        assertThat(saved.getCaregiver()).isEqualTo(caregiver);
    }
}
