package id.ac.ui.cs.advprog.bechat.service;

import id.ac.ui.cs.advprog.bechat.dto.SendMessageRequest;
import id.ac.ui.cs.advprog.bechat.model.ChatMessage;
import id.ac.ui.cs.advprog.bechat.model.ChatSession;
import id.ac.ui.cs.advprog.bechat.repository.ChatMessageRepository;
import id.ac.ui.cs.advprog.bechat.repository.ChatSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    void testSendMessage_success() {
        UUID sessionId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();

        ChatSession session = new ChatSession();
        session.setId(sessionId);
        session.setPacilian(senderId);

        SendMessageRequest request = new SendMessageRequest();
        request.setSessionId(sessionId);
        request.setContent("Halo Dunia");

        when(chatSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChatMessage result = chatService.sendMessage(request, senderId);

        assertEquals("Halo Dunia", result.getContent());
        assertEquals(senderId, result.getSenderId());
        assertEquals(session, result.getSession());
        assertFalse(result.isEdited());
        assertFalse(result.isDeleted());
    }

    @Test
    void testSendMessage_notMember_shouldThrowSecurityException() {
        UUID sessionId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        UUID anotherUser = UUID.randomUUID();

        ChatSession session = new ChatSession();
        session.setId(sessionId);
        session.setPacilian(anotherUser);
        session.setCaregiver(anotherUser);

        SendMessageRequest request = new SendMessageRequest();
        request.setSessionId(sessionId);
        request.setContent("Test");

        when(chatSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        assertThrows(SecurityException.class, () -> chatService.sendMessage(request, senderId));
    }

    @Test
    void testEditMessage_success() {
        UUID messageId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        ChatMessage message = new ChatMessage();
        message.setId(messageId);
        message.setSenderId(senderId);
        message.setContent("Old");

        when(chatMessageRepository.findById(messageId)).thenReturn(Optional.of(message));
        when(chatMessageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ChatMessage result = chatService.editMessage(messageId, "New", senderId);

        assertEquals("New", result.getContent());
        assertTrue(result.isEdited());
        assertNotNull(result.getEditedAt());
    }

    @Test
    void testEditMessage_wrongSender_shouldThrowSecurityException() {
        UUID messageId = UUID.randomUUID();
        ChatMessage message = new ChatMessage();
        message.setId(messageId);
        message.setSenderId(UUID.randomUUID());

        when(chatMessageRepository.findById(messageId)).thenReturn(Optional.of(message));

        assertThrows(SecurityException.class, () -> chatService.editMessage(messageId, "test", UUID.randomUUID()));
    }

    @Test
    void testDeleteMessage_success() {
        UUID messageId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        ChatMessage message = new ChatMessage();
        message.setId(messageId);
        message.setSenderId(senderId);
        message.setContent("Hello");

        when(chatMessageRepository.findById(messageId)).thenReturn(Optional.of(message));
        when(chatMessageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ChatMessage result = chatService.deleteMessage(messageId, senderId);

        assertTrue(result.isDeleted());
        assertEquals("Pesan telah dihapus", result.getContent());
    }

    @Test
    void testDeleteMessage_wrongSender_shouldThrowSecurityException() {
        UUID messageId = UUID.randomUUID();
        ChatMessage message = new ChatMessage();
        message.setId(messageId);
        message.setSenderId(UUID.randomUUID());

        when(chatMessageRepository.findById(messageId)).thenReturn(Optional.of(message));

        assertThrows(SecurityException.class, () -> chatService.deleteMessage(messageId, UUID.randomUUID()));
    }

    @Test
    void testGetMessages_notMember_shouldThrowSecurityException() {
        UUID sessionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ChatSession session = new ChatSession();
        session.setId(sessionId);
        session.setPacilian(UUID.randomUUID());
        session.setCaregiver(UUID.randomUUID());

        when(chatSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        assertThrows(SecurityException.class, () -> chatService.getMessages(sessionId, userId));
    }

    @Test
    void testGetSessionById_shouldReturnSession() {
        UUID sessionId = UUID.randomUUID();
        ChatSession session = new ChatSession();
        session.setId(sessionId);

        when(chatSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        ChatSession result = chatService.getSessionById(sessionId);
        assertEquals(sessionId, result.getId());
    }

    @Test
    void testGetSessionById_notFound_shouldThrow() {
        UUID sessionId = UUID.randomUUID();
        when(chatSessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> chatService.getSessionById(sessionId));
    }
}
