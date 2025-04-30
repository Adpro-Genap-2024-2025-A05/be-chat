package id.ac.ui.cs.advprog.bechat.functional;

import id.ac.ui.cs.advprog.bechat.model.builder.ChatMessage;
import id.ac.ui.cs.advprog.bechat.model.builder.ChatSession;
import id.ac.ui.cs.advprog.bechat.repository.ChatMessageRepository;
import id.ac.ui.cs.advprog.bechat.repository.ChatSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

    private ChatMessage message;

    @BeforeEach
    void setUp() {
        chatMessageRepository.deleteAll();
        chatSessionRepository.deleteAll();

        ChatSession session = new ChatSession();
        session.setId(UUID.randomUUID());
        session.setUser1Id(UUID.randomUUID());
        session.setUser2Id(UUID.randomUUID());
        session.setCreatedAt(LocalDateTime.now());
        chatSessionRepository.save(session);

        message = new ChatMessage();
        message.setId(UUID.randomUUID());
        message.setSession(session);
        message.setSenderId(session.getUser1Id());
        message.setContent("Ini pesan yang akan dihapus");
        message.setCreatedAt(LocalDateTime.now());
        message.setDeleted(false);
        message.setEdited(false);
        message = chatMessageRepository.save(message);
    }

    @Test
    void deleteMessage_shouldMarkMessageAsDeleted() throws Exception {
        mockMvc.perform(delete("/api/v1/chat/message/" + message.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deleted").value(true))
                .andExpect(jsonPath("$.content").value("Pesan telah dihapus"));

        ChatMessage updated = chatMessageRepository.findById(message.getId()).orElseThrow();
        assertThat(updated.isDeleted()).isTrue();
        assertThat(updated.getContent()).isEqualTo("Pesan telah dihapus");
    }
}
