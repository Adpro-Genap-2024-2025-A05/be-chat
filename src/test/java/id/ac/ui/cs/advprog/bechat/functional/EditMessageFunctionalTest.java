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
import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
class EditMessageFunctionalTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private ChatSession session;
    private ChatMessage message;

    @BeforeEach
    void setUp() {
        chatMessageRepository.deleteAll();
        chatSessionRepository.deleteAll();

        session = new ChatSession();
        session.setId(UUID.randomUUID());
        session.setUser1Id(UUID.randomUUID());
        session.setUser2Id(UUID.randomUUID());
        session.setCreatedAt(LocalDateTime.now());
        session = chatSessionRepository.save(session);

        message = new ChatMessage();
        message.setId(UUID.randomUUID());
        message.setSession(session);
        message.setSenderId(session.getUser1Id());
        message.setContent("Pesan tidak diedit");
        message.setCreatedAt(LocalDateTime.now());
        message.setDeleted(false);
        message.setEdited(false);
        message = chatMessageRepository.save(message);
    }

    @Test
    void testEditMessage_shouldUpdateMessageContent() throws Exception {
        EditMessageRequest request = new EditMessageRequest();
        request.setContent("Pesan diedit");

        mockMvc.perform(put("/api/v1/chat/message/" + message.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Pesan diedit"))
                .andExpect(jsonPath("$.edited").value(true));

        ChatMessage updated = chatMessageRepository.findById(message.getId()).orElseThrow();
        assertThat(updated.getContent()).isEqualTo("Pesan diedit");
        assertThat(updated.isEdited()).isTrue();
        assertThat(updated.getEditedAt()).isNotNull();
    }

    @Test
    void testEditDeletedMessage_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(delete("/api/v1/chat/message/" + message.getId()))
                .andExpect(status().isOk());

        EditMessageRequest request = new EditMessageRequest();
        request.setContent("Percobaan edit setelah dihapus");

        mockMvc.perform(put("/api/v1/chat/message/" + message.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testEditEditedMessage_shouldAllowReEdit() throws Exception {
        EditMessageRequest firstEdit = new EditMessageRequest();
        firstEdit.setContent("Pesan telah diedit pertama kali");

        mockMvc.perform(put("/api/v1/chat/message/" + message.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstEdit)))
                .andExpect(status().isOk());

        EditMessageRequest secondEdit = new EditMessageRequest();
        secondEdit.setContent("Pesan diedit ulang");

        mockMvc.perform(put("/api/v1/chat/message/" + message.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondEdit)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Pesan diedit ulang"))
                .andExpect(jsonPath("$.edited").value(true));
    }

    @Test
    void testEditMessage_whenMessageAlreadyDeleted_shouldThrowException() throws Exception {
        message.setDeleted(true);
        chatMessageRepository.save(message);
        message = chatMessageRepository.findById(message.getId()).orElseThrow(); // trigger @PostLoad

        EditMessageRequest request = new EditMessageRequest();
        request.setContent("Percobaan edit");

        mockMvc.perform(put("/api/v1/chat/message/" + message.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessageContaining("tidak bisa diedit"));
    }

}
