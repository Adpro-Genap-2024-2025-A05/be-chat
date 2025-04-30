package id.ac.ui.cs.advprog.bechat.service;

import id.ac.ui.cs.advprog.bechat.dto.SendMessageRequest;
import id.ac.ui.cs.advprog.bechat.model.builder.ChatMessage;
import id.ac.ui.cs.advprog.bechat.model.builder.ChatSession;
import id.ac.ui.cs.advprog.bechat.repository.ChatMessageRepository;
import id.ac.ui.cs.advprog.bechat.repository.ChatSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
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

        SendMessageRequest request = new SendMessageRequest();
        request.setSessionId(sessionId);
        request.setSenderId(senderId);
        request.setContent("Halo");

        when(chatSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(i -> i.getArgument(0));

        ChatMessage result = chatService.sendMessage(request);

        assertNotNull(result.getId());
        assertEquals("Halo", result.getContent());
        assertEquals(session, result.getSession());
        assertEquals(senderId, result.getSenderId());
        assertFalse(result.isEdited());
        assertFalse(result.isDeleted());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void testSendMessage_sessionNotFound_shouldThrow() {
        UUID sessionId = UUID.randomUUID();
        SendMessageRequest request = new SendMessageRequest();
        request.setSessionId(sessionId);

        when(chatSessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> chatService.sendMessage(request));
        assertEquals("Session not found", ex.getMessage());
    }

    @Test
    void testGetMessages_shouldReturnList() {
        UUID sessionId = UUID.randomUUID();
        ChatMessage msg = new ChatMessage();
        when(chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId)).thenReturn(List.of(msg));

        List<ChatMessage> result = chatService.getMessages(sessionId);

        assertEquals(1, result.size());
        assertSame(msg, result.get(0));
    }

    @Test
    void testEditMessage_success() {
        UUID messageId = UUID.randomUUID();
        ChatMessage msg = new ChatMessage();
        msg.setId(messageId);
        msg.setContent("Old");
        msg.setEdited(false);

        when(chatMessageRepository.findById(messageId)).thenReturn(Optional.of(msg));
        when(chatMessageRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ChatMessage result = chatService.editMessage(messageId, "New");

        assertEquals("New", result.getContent());
        assertTrue(result.isEdited());
        assertNotNull(result.getEditedAt());
    }

    @Test
    void testEditMessage_notFound_shouldThrow() {
        UUID messageId = UUID.randomUUID();
        when(chatMessageRepository.findById(messageId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> chatService.editMessage(messageId, "New"));
        assertEquals("Message not found", ex.getMessage());
    }

    @Test
    void testDeleteMessage_success() {
        UUID messageId = UUID.randomUUID();
        ChatMessage msg = new ChatMessage();
        msg.setId(messageId);
        msg.setContent("Before");
        msg.setDeleted(false);

        when(chatMessageRepository.findById(messageId)).thenReturn(Optional.of(msg));
        when(chatMessageRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ChatMessage result = chatService.deleteMessage(messageId);

        assertTrue(result.isDeleted());
        assertEquals("Pesan telah dihapus", result.getContent());
    }

    @Test
    void testDeleteMessage_notFound_shouldThrow() {
        UUID messageId = UUID.randomUUID();
        when(chatMessageRepository.findById(messageId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> chatService.deleteMessage(messageId));
        assertEquals("Message not found", ex.getMessage());
    }

    @Nested
    class ChatSessionServiceImplTest {

        private ChatSessionRepository chatSessionRepository;
        private ChatSessionServiceImpl chatSessionService;

        @BeforeEach
        void setUp() {
            chatSessionRepository = mock(ChatSessionRepository.class);
            chatSessionService = new ChatSessionServiceImpl(chatSessionRepository);
        }

        @Test
        void testCreateSession_shouldReturnSavedSession() {
            UUID user1 = UUID.randomUUID();
            UUID user2 = UUID.randomUUID();

            when(chatSessionRepository.save(any(ChatSession.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            ChatSession session = chatSessionService.createSession(user1, user2);

            assertEquals(user1, session.getUser1Id());
            assertEquals(user2, session.getUser2Id());
            assertNotNull(session.getId());
            assertNotNull(session.getCreatedAt());
        }

        @Test
        void testFindSession_shouldReturnIfFound() {
            UUID user1 = UUID.randomUUID();
            UUID user2 = UUID.randomUUID();

            ChatSession session = new ChatSession();
            session.setUser1Id(user1);
            session.setUser2Id(user2);

            when(chatSessionRepository.findAll()).thenReturn(List.of(session));

            Optional<ChatSession> result = chatSessionService.findSession(user1, user2);

            assertTrue(result.isPresent());
            assertEquals(session, result.get());
        }

        @Test
        void testFindSession_shouldReturnEmptyIfNotFound() {
            when(chatSessionRepository.findAll()).thenReturn(Collections.emptyList());

            Optional<ChatSession> result = chatSessionService.findSession(UUID.randomUUID(), UUID.randomUUID());

            assertTrue(result.isEmpty());
        }

        @Test
        void testGetSessionsByUser_shouldReturnSessions() {
            UUID userId = UUID.randomUUID();
            ChatSession session = new ChatSession();
            session.setUser1Id(userId);
            session.setUser2Id(UUID.randomUUID());

            when(chatSessionRepository.findByUser1IdOrUser2Id(userId, userId)).thenReturn(List.of(session));

            List<ChatSession> result = chatSessionService.getSessionsByUser(userId);

            assertEquals(1, result.size());
            assertEquals(session, result.get(0));
        }

        @Test
        void testFindSession_shouldReturnIfUsersAreSwapped() {
            UUID user1 = UUID.randomUUID();
            UUID user2 = UUID.randomUUID();

            ChatSession session = new ChatSession();
            session.setUser1Id(user2);
            session.setUser2Id(user1);

            when(chatSessionRepository.findAll()).thenReturn(List.of(session));

            Optional<ChatSession> result = chatSessionService.findSession(user1, user2);

            assertTrue(result.isPresent());
            assertEquals(session, result.get());
        }

        @Test
        void testDeleteSession_shouldCallRepository() {
            UUID sessionId = UUID.randomUUID();

            chatSessionService.deleteSession(sessionId);

            verify(chatSessionRepository, times(1)).deleteById(sessionId);
        }


    }
}
