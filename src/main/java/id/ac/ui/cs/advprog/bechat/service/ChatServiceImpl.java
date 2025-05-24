package id.ac.ui.cs.advprog.bechat.service;

import id.ac.ui.cs.advprog.bechat.dto.SendMessageRequest;
import id.ac.ui.cs.advprog.bechat.model.ChatMessage;
import id.ac.ui.cs.advprog.bechat.model.ChatSession;
import id.ac.ui.cs.advprog.bechat.repository.ChatMessageRepository;
import id.ac.ui.cs.advprog.bechat.repository.ChatSessionRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatSessionRepository chatSessionRepository;

    private final Counter sendMessageCounter;
    private final Counter sendMessageFailureCounter;
    private final Counter editMessageCounter;
    private final Counter deleteMessageCounter;
    private final Timer getMessagesTimer;

    @Override
    @Async("taskExecutor")
    public CompletableFuture<ChatMessage> sendMessage(SendMessageRequest dto, UUID senderId) {
        try {
            ChatSession session = chatSessionRepository.findById(dto.getSessionId())
                    .orElseThrow(() -> new RuntimeException("Session not found"));

            if (!session.getPacilian().equals(senderId) && !session.getCaregiver().equals(senderId)) {
                throw new SecurityException("You are not part of this session.");
            }

            ChatMessage message = new ChatMessage();
            message.setId(UUID.randomUUID());
            message.setSession(session);
            message.setSenderId(senderId);
            message.setContent(dto.getContent());
            message.setEdited(false);
            message.setDeleted(false);

            ChatMessage saved = chatMessageRepository.save(message);
            sendMessageCounter.increment();
            return CompletableFuture.completedFuture(saved);
        } catch (Exception e) {
            sendMessageFailureCounter.increment();
            throw e;
        }
    }

    @Override
    @Async("taskExecutor")
    public CompletableFuture<List<ChatMessage>> getMessages(UUID sessionId, UUID userId) {
        return CompletableFuture.supplyAsync(() -> getMessagesTimer.<List<ChatMessage>>record(() -> {
            try {
                ChatSession session = getSessionById(sessionId);

                if (!session.getPacilian().equals(userId) && !session.getCaregiver().equals(userId)) {
                    throw new SecurityException("You do not have access to this session.");
                }

                return chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
            } catch (Exception e) {
                throw new RuntimeException("Access denied or other failure in getMessages", e);
            }
        }));
    }


    @Override
    @Async("taskExecutor")
    public CompletableFuture<ChatMessage> editMessage(UUID messageId, String newContent, UUID userId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (!message.getSenderId().equals(userId)) {
            throw new SecurityException("You can only edit your own messages.");
        }

        message.edit(newContent);
        ChatMessage saved = chatMessageRepository.save(message);
        editMessageCounter.increment();
        return CompletableFuture.completedFuture(saved);
    }

    @Override
    @Async("taskExecutor")
    public CompletableFuture<ChatMessage> deleteMessage(UUID messageId, UUID userId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (!message.getSenderId().equals(userId)) {
            throw new SecurityException("You can only delete your own messages.");
        }

        message.delete();
        ChatMessage saved = chatMessageRepository.save(message);
        deleteMessageCounter.increment();
        return CompletableFuture.completedFuture(saved);
    }

    @Override
    public ChatSession getSessionById(UUID sessionId) {
        return chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
    }

    @Async("taskExecutor")
    public CompletableFuture<Optional<ChatSession>> findSessionById(UUID sessionId) {
        return CompletableFuture.completedFuture(chatSessionRepository.findById(sessionId));
    }
}
