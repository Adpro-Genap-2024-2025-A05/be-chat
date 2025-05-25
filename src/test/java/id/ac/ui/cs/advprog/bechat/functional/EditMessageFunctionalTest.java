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
class EditMessageFunctionalTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ChatSessionRepository chatSessionRepository;
    @Autowired private ChatMessageRepository chatMessageRepository;
    @Autowired private ObjectMapper objectMapper;

    private ChatSession session;
    private ChatMessage message;
    private static final String FAKE_TOKEN = "Bearer faketoken";

    @BeforeEach
    void setUp() {
        chatMessageRepository.deleteAll();
        chatSessionRepository.deleteAll();

        UUID pacilian = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID caregiver = UUID.fromString("22222222-2222-2222-2222-222222222222");

        session = new ChatSession();
        session.setId(UUID.randomUUID());
        session.setPacilian(pacilian);
        session.setCaregiver(caregiver);
        session.setCreatedAt(LocalDateTime.now());
        session = chatSessionRepository.save(session);

        message = new ChatMessage();
        message.setId(UUID.randomUUID());
        message.setSession(session);
        message.setSenderId(pacilian);
        message.setContent("Pesan tidak diedit");
        message.setCreatedAt(new Date());
        message.setDeleted(false);
        message.setEdited(false);
        message = chatMessageRepository.save(message);
    }

    @Test
    void testEditMessage_shouldUpdateMessageContent() throws Exception {
        EditMessageRequest request = new EditMessageRequest();
        request.setContent("Pesan diedit");

        mockMvc.perform(put("/api/chat/message/" + message.getId())
                        .header("Authorization", FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").value("Pesan diedit"))
                .andExpect(jsonPath("$.data.edited").value(true));

        ChatMessage updated = chatMessageRepository.findById(message.getId()).orElseThrow();
        assertThat(updated.getContent()).isEqualTo("Pesan diedit");
        assertThat(updated.isEdited()).isTrue();
        assertThat(updated.getEditedAt()).isNotNull();
    }

    @Test
    void testEditDeletedMessage_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(delete("/api/chat/message/" + message.getId())
                        .header("Authorization", FAKE_TOKEN))
                .andExpect(status().isOk());

        EditMessageRequest request = new EditMessageRequest();
        request.setContent("Percobaan edit setelah dihapus");

        mockMvc.perform(put("/api/chat/message/" + message.getId())
                        .header("Authorization", FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Message sudah dihapus"));
    }

    @Test
    void testEditEditedMessage_shouldAllowReEdit() throws Exception {
        EditMessageRequest firstEdit = new EditMessageRequest();
        firstEdit.setContent("Pesan telah diedit pertama kali");

        mockMvc.perform(put("/api/chat/message/" + message.getId())
                        .header("Authorization", FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstEdit)))
                .andExpect(status().isOk());

        EditMessageRequest secondEdit = new EditMessageRequest();
        secondEdit.setContent("Pesan diedit ulang");

        mockMvc.perform(put("/api/chat/message/" + message.getId())
                        .header("Authorization", FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondEdit)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").value("Pesan diedit ulang"))
                .andExpect(jsonPath("$.data.edited").value(true));
    }

    @Test
    void testEditMessage_whenAlreadyDeleted_shouldThrowException() throws Exception {
        message.setDeleted(true);
        chatMessageRepository.save(message);
        message = chatMessageRepository.findById(message.getId()).orElseThrow(); // force reload

        EditMessageRequest request = new EditMessageRequest();
        request.setContent("Percobaan edit");

        mockMvc.perform(put("/api/chat/message/" + message.getId())
                        .header("Authorization", FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Message sudah dihapus"));
    }
}
