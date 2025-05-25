package id.ac.ui.cs.advprog.bechat.service;

import id.ac.ui.cs.advprog.bechat.dto.SendMessageRequest;
import id.ac.ui.cs.advprog.bechat.model.ChatMessage;
import id.ac.ui.cs.advprog.bechat.model.ChatSession;
import id.ac.ui.cs.advprog.bechat.repository.ChatMessageRepository;
import id.ac.ui.cs.advprog.bechat.repository.ChatSessionRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatServiceImpl.class);

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
        logger.info("Attempting to send message in session: {}, sender: {}", dto.getSessionId(), senderId);

        try {
            ChatSession session = chatSessionRepository.findById(dto.getSessionId())
                    .orElseThrow(() -> {
                        logger.warn("Session {} not found for sending message", dto.getSessionId());
                        return new RuntimeException("Session not found");
                    });

            if (!session.getPacilian().equals(senderId) && !session.getCaregiver().equals(senderId)) {
                logger.warn("Unauthorized sender {} for session {}", senderId, dto.getSessionId());
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
            logger.info("Message successfully sent with ID: {}", saved.getId());

            return CompletableFuture.completedFuture(saved);
        } catch (Exception e) {
            sendMessageFailureCounter.increment();
            logger.error("Failed to send message for session: {}, sender: {}. Error: {}", dto.getSessionId(), senderId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Async("taskExecutor")
    public CompletableFuture<List<ChatMessage>> getMessages(UUID sessionId, UUID userId) {
        logger.info("Fetching messages for session: {} and user: {}", sessionId, userId);

        return CompletableFuture.supplyAsync(() -> getMessagesTimer.<List<ChatMessage>>record(() -> {
            try {
                ChatSession session = getSessionById(sessionId);

                if (!session.getPacilian().equals(userId) && !session.getCaregiver().equals(userId)) {
                    logger.warn("User {} unauthorized to access session {}", userId, sessionId);
                    throw new SecurityException("You do not have access to this session.");
                }

                List<ChatMessage> messages = chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
                logger.debug("Retrieved {} messages for session {}", messages.size(), sessionId);
                return messages;
            } catch (Exception e) {
                logger.error("Error fetching messages for session {} by user {}: {}", sessionId, userId, e.getMessage(), e);
                throw new RuntimeException("Access denied or other failure in getMessages", e);
            }
        }));
    }

    @Override
    @Async("taskExecutor")
    public CompletableFuture<ChatMessage> editMessage(UUID messageId, String newContent, UUID userId) {
        logger.info("User {} is attempting to edit message {}", userId, messageId);

        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> {
                    logger.warn("Message {} not found for editing", messageId);
                    return new RuntimeException("Message not found");
                });

        if (!message.getSenderId().equals(userId)) {
            logger.warn("User {} is not the sender of message {} and cannot edit it", userId, messageId);
            throw new SecurityException("You can only edit your own messages.");
        }

        message.edit(newContent);
        ChatMessage saved = chatMessageRepository.save(message);
        editMessageCounter.increment();

        logger.info("Message {} successfully edited by user {}", messageId, userId);
        return CompletableFuture.completedFuture(saved);
    }

    @Override
    @Async("taskExecutor")
    public CompletableFuture<ChatMessage> deleteMessage(UUID messageId, UUID userId) {
        logger.info("User {} is attempting to delete message {}", userId, messageId);

        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> {
                    logger.warn("Message {} not found for deletion", messageId);
                    return new RuntimeException("Message not found");
                });

        if (!message.getSenderId().equals(userId)) {
            logger.warn("User {} is not the sender of message {} and cannot delete it", userId, messageId);
            throw new SecurityException("You can only delete your own messages.");
        }

        message.delete();
        ChatMessage saved = chatMessageRepository.save(message);
        deleteMessageCounter.increment();

        logger.info("Message {} successfully deleted by user {}", messageId, userId);
        return CompletableFuture.completedFuture(saved);
    }

    @Override
    public ChatSession getSessionById(UUID sessionId) {
        logger.debug("Fetching session by ID: {}", sessionId);

        return chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> {
                    logger.warn("Session {} not found", sessionId);
                    return new RuntimeException("Session not found");
                });
    }

    @Async("taskExecutor")
    public CompletableFuture<Optional<ChatSession>> findSessionById(UUID sessionId) {
        logger.debug("Asynchronously finding session by ID: {}", sessionId);
        return CompletableFuture.completedFuture(chatSessionRepository.findById(sessionId));
    }
}
