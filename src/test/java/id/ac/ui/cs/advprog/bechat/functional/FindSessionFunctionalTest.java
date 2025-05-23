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
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class FindSessionFunctionalTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID pacilian;
    private UUID caregiver;
    private ChatSession session;

    private static final String FAKE_TOKEN = "Bearer faketoken";

    @BeforeEach
    void setUp() {
        chatSessionRepository.deleteAll();

        pacilian = UUID.fromString("11111111-1111-1111-1111-111111111111");
        caregiver = UUID.fromString("22222222-2222-2222-2222-222222222222");

        session = new ChatSession();
        session.setId(UUID.randomUUID());
        session.setPacilian(pacilian);
        session.setCaregiver(caregiver);
        session.setCreatedAt(LocalDateTime.now());

        chatSessionRepository.save(session);
    }

    @Test
    void findSession_shouldReturnSessionIfExists() throws Exception {
        mockMvc.perform(get("/api/chat/session/find")
                        .param("user1", pacilian.toString())
                        .param("user2", caregiver.toString())
                        .header("Authorization", FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pacilian").value(pacilian.toString()))
                .andExpect(jsonPath("$.data.caregiver").value(caregiver.toString()));
    }

    @Test
    void findSession_shouldReturnNotFoundIfSessionDoesNotExist() throws Exception {
        UUID otherUser1 = UUID.randomUUID();
        UUID otherUser2 = UUID.randomUUID();

        mockMvc.perform(get("/api/chat/session/find")
                        .param("user1", otherUser1.toString())
                        .param("user2", otherUser2.toString())
                        .header("Authorization", FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Session not found"));
    }
}
