package id.ac.ui.cs.advprog.bechat.service;

import id.ac.ui.cs.advprog.bechat.dto.SendMessageRequest;
import id.ac.ui.cs.advprog.bechat.model.builder.ChatMessage;
import id.ac.ui.cs.advprog.bechat.model.builder.ChatSession;
import id.ac.ui.cs.advprog.bechat.repository.ChatMessageRepository;
import id.ac.ui.cs.advprog.bechat.repository.ChatSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatServiceImplTest {

    private ChatMessageRepository chatMessageRepository;
    private ChatSessionRepository chatSessionRepository;
    private ChatServiceImpl chatService;

    @BeforeEach
    void setUp() {
        chatMessageRepository = mock(ChatMessageRepository.class);
        chatSessionRepository = mock(ChatSessionRepository.class);
        chatService = new ChatServiceImpl(chatMessageRepository, chatSessionRepository);
    }

    @Test
    void testSendMessage_shouldReturnSavedMessage() {
        UUID sessionId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        ChatSession session = new ChatSession();
        session.setId(sessionId);

        SendMessageRequest request = new SendMessageRequest();
        request.setSessionId(sessionId);
        request.setSenderId(senderId);
        request.setContent("Halo ini TDD");

        when(chatSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(chatMessageRepository.save(Mockito.any(ChatMessage.class))).thenAnswer(i -> i.getArgument(0));

        ChatMessage result = chatService.sendMessage(request);

        assertNotNull(result.getId());
        assertEquals("Halo ini TDD", result.getContent());
        assertEquals(senderId, result.getSenderId());
        assertEquals(session, result.getSession());
        assertFalse(result.isEdited());
        assertFalse(result.isDeleted());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void editMessage_shouldUpdateContent() {
        UUID messageId = UUID.randomUUID();
        String newContent = "Pesan baru yang sudah diedit";
        ChatMessage existing = new ChatMessage();
        existing.setId(messageId);
        existing.setContent("Pesan lama");
        existing.setEdited(false);
        existing.setEditedAt(null);
        existing.setCreatedAt(LocalDateTime.now());

        when(chatMessageRepository.findById(messageId)).thenReturn(Optional.of(existing));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(i -> i.getArgument(0));

        ChatMessage updated = chatService.editMessage(messageId, newContent);

        assertEquals(newContent, updated.getContent());
        assertTrue(updated.isEdited());
        assertNotNull(updated.getEditedAt());
    }

    @Test
    void deleteMessage_shouldMarkAsDeleted() {
        UUID messageId = UUID.randomUUID();
        ChatMessage existing = new ChatMessage();
        existing.setId(messageId);
        existing.setContent("Ini pesan sebelum dihapus");
        existing.setDeleted(false);
        existing.setCreatedAt(LocalDateTime.now());

        when(chatMessageRepository.findById(messageId)).thenReturn(Optional.of(existing));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChatMessage deleted = chatService.deleteMessage(messageId);

        assertTrue(deleted.isDeleted());
        assertEquals("Pesan telah dihapus", deleted.getContent());
    }

}
