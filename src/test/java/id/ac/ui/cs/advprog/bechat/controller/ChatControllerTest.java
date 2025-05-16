package id.ac.ui.cs.advprog.bechat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.bechat.dto.EditMessageRequest;
import id.ac.ui.cs.advprog.bechat.dto.SendMessageRequest;
import id.ac.ui.cs.advprog.bechat.dto.TokenVerificationResponseDto;
import id.ac.ui.cs.advprog.bechat.model.ChatMessage;
import id.ac.ui.cs.advprog.bechat.model.enums.Role;
import id.ac.ui.cs.advprog.bechat.service.ChatService;
import id.ac.ui.cs.advprog.bechat.service.TokenVerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable Spring Security filters for test
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatService;

    @MockBean
    private TokenVerificationService tokenVerificationService;

    @Autowired
    private ObjectMapper objectMapper;

    private ChatMessage dummyMessage;
    private UUID dummyUserId;
    private static final String DUMMY_TOKEN = "faketoken";

    @BeforeEach
    void setUp() {
        dummyUserId = UUID.randomUUID();

        dummyMessage = new ChatMessage();
        dummyMessage.setId(UUID.randomUUID());
        dummyMessage.setSenderId(dummyUserId);
        dummyMessage.setContent("Halo Dunia");
        dummyMessage.setCreatedAt(new Date());

        Mockito.when(tokenVerificationService.verifyToken(DUMMY_TOKEN))
                .thenReturn(TokenVerificationResponseDto.builder()
                        .userId(dummyUserId.toString())
                        .valid(true)
                        .role(Role.PACILIAN)
                        .email("test@example.com")
                        .expiresIn(3600L)
                        .build());
    }

    @Test
    void testSendMessage() throws Exception {
        SendMessageRequest request = new SendMessageRequest();
        request.setSessionId(UUID.randomUUID());
        request.setContent("Halo Dunia");

        Mockito.when(chatService.sendMessage(any(SendMessageRequest.class), eq(dummyUserId)))
                .thenReturn(dummyMessage);

        mockMvc.perform(post("/chat/send")
                        .header("Authorization", "Bearer " + DUMMY_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.content", is("Halo Dunia")));
    }

    @Test
    void testGetMessages() throws Exception {
        UUID sessionId = UUID.randomUUID();
        List<ChatMessage> messages = Collections.singletonList(dummyMessage);

        Mockito.when(chatService.getMessages(eq(sessionId), eq(dummyUserId)))
                .thenReturn(messages);

        mockMvc.perform(get("/chat/session/{id}", sessionId)
                        .header("Authorization", "Bearer " + DUMMY_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].content", is("Halo Dunia")));
    }

    @Test
    void testEditMessage() throws Exception {
        UUID messageId = UUID.randomUUID();
        EditMessageRequest request = new EditMessageRequest();
        request.setContent("Pesan diedit");

        dummyMessage.setContent("Pesan diedit");

        Mockito.when(chatService.editMessage(eq(messageId), eq("Pesan diedit"), eq(dummyUserId)))
                .thenReturn(dummyMessage);

        mockMvc.perform(put("/chat/message/{id}", messageId)
                        .header("Authorization", "Bearer " + DUMMY_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", is("Pesan diedit")));
    }

    @Test
    void testDeleteMessage() throws Exception {
        UUID messageId = UUID.randomUUID();
        dummyMessage.setContent("Pesan telah dihapus");

        Mockito.when(chatService.deleteMessage(eq(messageId), eq(dummyUserId)))
                .thenReturn(dummyMessage);

        mockMvc.perform(delete("/chat/message/{id}", messageId)
                        .header("Authorization", "Bearer " + DUMMY_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", is("Pesan telah dihapus")));
    }
}
