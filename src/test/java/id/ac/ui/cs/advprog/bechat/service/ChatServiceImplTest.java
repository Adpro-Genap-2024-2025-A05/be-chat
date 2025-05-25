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

    @SuppressWarnings("unchecked")
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

        SendMessageRequest request = new SendMessageRequest();
        request.setSessionId(sessionId);
        request.setContent("Halo Dunia");

        when(chatSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChatMessage result = chatService.sendMessage(request, senderId).get();

        assertEquals("Halo Dunia", result.getContent());
        assertEquals(senderId, result.getSenderId());
        assertEquals(session, result.getSession());
        assertFalse(result.isEdited());
        assertFalse(result.isDeleted());

        verify(sendMessageCounter).increment();
        verify(sendMessageFailureCounter, never()).increment();
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

        assertThrows(SecurityException.class, () -> chatService.sendMessage(request, senderId).join());
        verify(sendMessageFailureCounter).increment();
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

        ChatMessage result = chatService.editMessage(messageId, "New", senderId).get();

        assertEquals("New", result.getContent());
        assertTrue(result.isEdited());
        verify(editMessageCounter).increment();
    }

    @Test
    void testEditMessage_wrongSender_shouldThrowSecurityException() {
        UUID messageId = UUID.randomUUID();
        ChatMessage message = new ChatMessage();
        message.setId(messageId);
        message.setSenderId(UUID.randomUUID());

        when(chatMessageRepository.findById(messageId)).thenReturn(Optional.of(message));

        assertThrows(SecurityException.class, () -> chatService.editMessage(messageId, "test", UUID.randomUUID()).join());
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

        ChatMessage result = chatService.deleteMessage(messageId, senderId).get();

        assertTrue(result.isDeleted());
        assertEquals("Pesan telah dihapus", result.getContent());
        verify(deleteMessageCounter).increment();
    }

    @Test
    void testDeleteMessage_wrongSender_shouldThrowSecurityException() {
        UUID messageId = UUID.randomUUID();
        ChatMessage message = new ChatMessage();
        message.setId(messageId);
        message.setSenderId(UUID.randomUUID());

        when(chatMessageRepository.findById(messageId)).thenReturn(Optional.of(message));

        assertThrows(SecurityException.class, () -> chatService.deleteMessage(messageId, UUID.randomUUID()).join());
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

        CompletionException exception = assertThrows(CompletionException.class, () -> {
            chatService.getMessages(sessionId, userId).join();
        });

        assertNotNull(exception.getCause());
        assertTrue(exception.getCause().getCause() instanceof SecurityException);
        assertEquals("You do not have access to this session.", exception.getCause().getCause().getMessage());
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
    @SuppressWarnings("unchecked")
    @Test
    void testGetMessages_success() throws Exception {
        UUID sessionId = UUID.randomUUID();
        UUID userId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

        ChatSession session = new ChatSession();
        session.setId(sessionId);
        session.setPacilian(userId); // userId valid sebagai pacilian
        session.setCaregiver(UUID.randomUUID());

        List<ChatMessage> mockMessages = List.of(
                new ChatMessage(), new ChatMessage()
        );

        when(chatSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId)).thenReturn(mockMessages);

        List<ChatMessage> result = chatService.getMessages(sessionId, userId).get();

        assertEquals(2, result.size());
        verify(getMessagesTimer).record(any(Supplier.class)); // Verifikasi timer
    }

}
