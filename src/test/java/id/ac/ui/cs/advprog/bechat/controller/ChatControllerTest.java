package id.ac.ui.cs.advprog.bechat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.bechat.dto.EditMessageRequest;
import id.ac.ui.cs.advprog.bechat.dto.SendMessageRequest;
import id.ac.ui.cs.advprog.bechat.model.builder.ChatMessage;
import id.ac.ui.cs.advprog.bechat.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatService;

    @Autowired
    private ObjectMapper objectMapper;

    private ChatMessage dummyMessage;

    @BeforeEach
    void setUp() {
        dummyMessage = new ChatMessage();
        dummyMessage.setId(UUID.randomUUID());
        dummyMessage.setSenderId(UUID.randomUUID());
        dummyMessage.setContent("Halo Dunia");
        dummyMessage.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testSendMessage() throws Exception {
        SendMessageRequest request = new SendMessageRequest();
        request.setSessionId(UUID.randomUUID());
        request.setSenderId(dummyMessage.getSenderId());
        request.setContent("Halo Dunia");

        Mockito.when(chatService.sendMessage(any(SendMessageRequest.class))).thenReturn(dummyMessage);

        mockMvc.perform(post("/api/v1/chat/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is("Halo Dunia")));
    }

    @Test
    void testGetMessages() throws Exception {
        UUID sessionId = UUID.randomUUID();
        Mockito.when(chatService.getMessages(eq(sessionId)))
                .thenReturn(Collections.singletonList(dummyMessage));

        mockMvc.perform(get("/api/v1/chat/session/{id}", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content", is("Halo Dunia")));
    }

    @Test
    void testEditMessage() throws Exception {
        UUID messageId = UUID.randomUUID();
        EditMessageRequest request = new EditMessageRequest();
        request.setContent("Pesan diedit");

        dummyMessage.setContent("Pesan diedit");
        Mockito.when(chatService.editMessage(eq(messageId), eq("Pesan diedit")))
                .thenReturn(dummyMessage);

        mockMvc.perform(put("/api/v1/chat/message/{id}", messageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is("Pesan diedit")));
    }

    @Test
    void testDeleteMessage() throws Exception {
        UUID messageId = UUID.randomUUID();
        dummyMessage.setContent("Pesan telah dihapus");

        Mockito.when(chatService.deleteMessage(eq(messageId))).thenReturn(dummyMessage);

        mockMvc.perform(delete("/api/v1/chat/message/{id}", messageId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is("Pesan telah dihapus")));
    }
}
