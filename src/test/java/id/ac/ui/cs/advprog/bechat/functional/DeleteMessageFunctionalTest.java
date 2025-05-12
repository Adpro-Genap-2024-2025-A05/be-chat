package id.ac.ui.cs.advprog.bechat.functional;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.bechat.dto.EditMessageRequest;
import id.ac.ui.cs.advprog.bechat.model.ChatMessage;
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
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class DeleteMessageFunctionalTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private ChatMessage message;
    private UUID senderId;
    private static final String FAKE_TOKEN = "Bearer faketoken";

    @BeforeEach
    void setUp() {
        chatMessageRepository.deleteAll();
        chatSessionRepository.deleteAll();

        senderId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID caregiverId = UUID.fromString("22222222-2222-2222-2222-222222222222");

        ChatSession session = new ChatSession();
        session.setId(UUID.randomUUID());
        session.setPacilian(senderId);
        session.setCaregiver(caregiverId);
        session.setCreatedAt(LocalDateTime.now());
        chatSessionRepository.save(session);

        message = new ChatMessage();
        message.setId(UUID.randomUUID());
        message.setSession(session);
        message.setSenderId(senderId);
        message.setContent("Ini pesan yang akan dihapus");
        message.setCreatedAt(new Date());
        message.setDeleted(false);
        message.setEdited(false);
        message = chatMessageRepository.save(message);
    }

    @Test
    void deleteMessage_shouldMarkMessageAsDeleted() throws Exception {
        mockMvc.perform(delete("/chat/message/" + message.getId())
                        .header("Authorization", FAKE_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.deleted").value(true))
                .andExpect(jsonPath("$.data.content").value("Pesan telah dihapus"));

        ChatMessage updated = chatMessageRepository.findById(message.getId()).orElseThrow();
        assertThat(updated.isDeleted()).isTrue();
        assertThat(updated.getContent()).isEqualTo("Pesan telah dihapus");
    }

    @Test
    void testDeleteAfterEdit_shouldWork() throws Exception {
        EditMessageRequest editRequest = new EditMessageRequest();
        editRequest.setContent("Sudah diedit sebelumnya");

        mockMvc.perform(put("/chat/message/" + message.getId())
                        .header("Authorization", FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/chat/message/" + message.getId())
                        .header("Authorization", FAKE_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.deleted").value(true))
                .andExpect(jsonPath("$.data.content").value("Pesan telah dihapus"));
    }

    @Test
    void testDelete_whenAlreadyDeleted_shouldThrowException() throws Exception {
        message.setDeleted(true);
        chatMessageRepository.save(message);

        mockMvc.perform(delete("/chat/message/" + message.getId())
                        .header("Authorization", FAKE_TOKEN))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Message sudah dihapus"));
    }
}
