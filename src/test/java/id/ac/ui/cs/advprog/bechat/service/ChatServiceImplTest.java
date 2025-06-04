package id.ac.ui.cs.advprog.bechat.service;

import id.ac.ui.cs.advprog.bechat.dto.SendMessageRequest;
import id.ac.ui.cs.advprog.bechat.model.ChatMessage;
import id.ac.ui.cs.advprog.bechat.model.ChatSession;
import id.ac.ui.cs.advprog.bechat.repository.ChatMessageRepository;
import id.ac.ui.cs.advprog.bechat.repository.ChatSessionRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ChatServiceImplTest {

    private ChatMessageRepository chatMessageRepository;
    private ChatSessionRepository chatSessionRepository;

    private Counter sendMessageCounter;
    private Counter sendMessageFailureCounter;
    private Counter editMessageCounter;
    private Counter deleteMessageCounter;
    private Timer getMessagesTimer;

    private ChatServiceImpl chatService;

    @BeforeEach
    void setUp() {
        chatMessageRepository = mock(ChatMessageRepository.class);
        chatSessionRepository = mock(ChatSessionRepository.class);
        sendMessageCounter = mock(Counter.class);
        sendMessageFailureCounter = mock(Counter.class);
        editMessageCounter = mock(Counter.class);
        deleteMessageCounter = mock(Counter.class);
        getMessagesTimer = mock(Timer.class);

        when(getMessagesTimer.<List<ChatMessage>>record(any(Supplier.class)))
            .thenAnswer(invocation -> {
                Supplier<List<ChatMessage>> supplier = (Supplier<List<ChatMessage>>) invocation.getArgument(0);
                return supplier.get();
            });

        chatService = new ChatServiceImpl(
            chatMessageRepository,
            chatSessionRepository,
            sendMessageCounter,
            sendMessageFailureCounter,
            editMessageCounter,
            deleteMessageCounter,
            getMessagesTimer
        );
    }

    @Test
    void testSendMessage_success() throws Exception {
        UUID sessionId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();

        ChatSession session = new ChatSession();
        session.setId(sessionId);
        session.setPacilian(senderId);
        session.setCaregiver(UUID.randomUUID());

        SendMessageRequest request = new SendMessageRequest();
        request.setSessionId(sessionId);
        request.setContent("Halo Dunia");

        when(chatSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Since session exists and sender is valid, this returns a CompletableFuture<ChatMessage> which completes normally.
        CompletableFuture<ChatMessage> future = chatService.sendMessage(request, senderId);
        ChatMessage result = future.get(); // wait for completion

        assertEquals("Halo Dunia", result.getContent());
        assertEquals(senderId, result.getSenderId());
        assertEquals(session, result.getSession());
        assertFalse(result.isEdited());
        assertFalse(result.isDeleted());

        verify(sendMessageCounter).increment();
        verify(sendMessageFailureCounter, never()).increment();
    }

    @Test
    void testSendMessage_sessionNotFound_throwsRuntimeException() {
        UUID sessionId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();

        SendMessageRequest request = new SendMessageRequest();
        request.setSessionId(sessionId);
        request.setContent("Hello");

        when(chatSessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> chatService.sendMessage(request, senderId));
        assertEquals("Session not found", ex.getMessage());

        verify(sendMessageFailureCounter).increment();
        verify(sendMessageCounter, never()).increment();
    }

    @Test
    void testSendMessage_notMember_shouldThrowSecurityException() {
        UUID sessionId = UUID.randomUUID();
        UUID senderId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID otherUser = UUID.fromString("22222222-2222-2222-2222-222222222222");

        ChatSession session = new ChatSession();
        session.setId(sessionId);
        session.setPacilian(otherUser);
        session.setCaregiver(otherUser);

        SendMessageRequest request = new SendMessageRequest();
        request.setSessionId(sessionId);
        request.setContent("Test");

        when(chatSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        SecurityException ex = assertThrows(SecurityException.class,
            () -> chatService.sendMessage(request, senderId));
        assertEquals("You are not part of this session.", ex.getMessage());

        verify(sendMessageFailureCounter).increment();
        verify(sendMessageCounter, never()).increment();
    }

    @Test
    void testEditMessage_success() throws Exception {
        UUID messageId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        ChatMessage message = new ChatMessage();
        message.setId(messageId);
        message.setSenderId(senderId);
        message.setContent("Old");

        when(chatMessageRepository.findById(messageId)).thenReturn(Optional.of(message));
        when(chatMessageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CompletableFuture<ChatMessage> future = chatService.editMessage(messageId, "New", senderId);
        ChatMessage result = future.get();

        assertEquals("New", result.getContent());
        assertTrue(result.isEdited());
        verify(editMessageCounter).increment();
    }

    @Test
    void testEditMessage_messageNotFound_throwsRuntimeException() {
        UUID messageId = UUID.randomUUID();

        when(chatMessageRepository.findById(messageId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> chatService.editMessage(messageId, "New", UUID.randomUUID()));
        assertEquals("Message not found", ex.getMessage());
        verify(editMessageCounter, never()).increment();
    }

    @Test
    void testEditMessage_wrongSender_shouldThrowSecurityException() {
        UUID messageId = UUID.randomUUID();
        ChatMessage message = new ChatMessage();
        message.setId(messageId);
        message.setSenderId(UUID.randomUUID());

        when(chatMessageRepository.findById(messageId)).thenReturn(Optional.of(message));

        SecurityException ex = assertThrows(SecurityException.class,
            () -> chatService.editMessage(messageId, "test", UUID.randomUUID()));
        assertEquals("You can only edit your own messages.", ex.getMessage());
        verify(editMessageCounter, never()).increment();
    }

    @Test
    void testDeleteMessage_success() throws Exception {
        UUID messageId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        ChatMessage message = new ChatMessage();
        message.setId(messageId);
        message.setSenderId(senderId);
        message.setContent("Hello");

        when(chatMessageRepository.findById(messageId)).thenReturn(Optional.of(message));
        when(chatMessageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CompletableFuture<ChatMessage> future = chatService.deleteMessage(messageId, senderId);
        ChatMessage result = future.get();

        assertTrue(result.isDeleted());
        verify(deleteMessageCounter).increment();
    }

    @Test
    void testDeleteMessage_messageNotFound_throwsRuntimeException() {
        UUID messageId = UUID.randomUUID();

        when(chatMessageRepository.findById(messageId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> chatService.deleteMessage(messageId, UUID.randomUUID()));
        assertEquals("Message not found", ex.getMessage());
        verify(deleteMessageCounter, never()).increment();
    }

    @Test
    void testDeleteMessage_wrongSender_shouldThrowSecurityException() {
        UUID messageId = UUID.randomUUID();
        ChatMessage message = new ChatMessage();
        message.setId(messageId);
        message.setSenderId(UUID.randomUUID());

        when(chatMessageRepository.findById(messageId)).thenReturn(Optional.of(message));

        SecurityException ex = assertThrows(SecurityException.class,
            () -> chatService.deleteMessage(messageId, UUID.randomUUID()));
        assertEquals("You can only delete your own messages.", ex.getMessage());
        verify(deleteMessageCounter, never()).increment();
    }

    @Test
    void testGetMessages_notMember_shouldThrowSecurityException() {
        UUID sessionId = UUID.randomUUID();
        UUID userId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        UUID pacilian = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        UUID caregiver = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

        ChatSession session = new ChatSession();
        session.setId(sessionId);
        session.setPacilian(pacilian);
        session.setCaregiver(caregiver);

        when(chatSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        CompletionException ex = assertThrows(CompletionException.class,
            () -> chatService.getMessages(sessionId, userId).join());
        assertTrue(ex.getCause().getCause() instanceof SecurityException);
        assertEquals("You do not have access to this session.", ex.getCause().getCause().getMessage());
    }

    @Test
    void testGetMessages_sessionNotFound_throwsRuntimeException() {
        UUID sessionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(chatSessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        CompletionException ex = assertThrows(CompletionException.class,
            () -> chatService.getMessages(sessionId, userId).join());
        assertTrue(ex.getCause().getCause() instanceof RuntimeException);
        assertEquals("Session not found", ex.getCause().getCause().getMessage());
    }

    @Test
    void testGetMessages_success() throws Exception {
        UUID sessionId = UUID.randomUUID();
        UUID userId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

        ChatSession session = new ChatSession();
        session.setId(sessionId);
        session.setPacilian(userId);
        session.setCaregiver(UUID.randomUUID());

        List<ChatMessage> mockMessages = List.of(new ChatMessage(), new ChatMessage());

        when(chatSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId)).thenReturn(mockMessages);

        List<ChatMessage> result = chatService.getMessages(sessionId, userId).get();
        assertEquals(2, result.size());
        verify(getMessagesTimer).record(any(Supplier.class));
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

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> chatService.getSessionById(sessionId));
        assertEquals("Session not found", ex.getMessage());
    }

    @Test
    void testFindSessionById_shouldReturnOptional() throws Exception {
        UUID sessionId = UUID.randomUUID();
        ChatSession session = new ChatSession();
        session.setId(sessionId);

        when(chatSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        CompletableFuture<Optional<ChatSession>> futureOpt = chatService.findSessionById(sessionId);
        Optional<ChatSession> opt = futureOpt.get();
        assertTrue(opt.isPresent());
        assertEquals(session, opt.get());
    }
}
