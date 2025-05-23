package id.ac.ui.cs.advprog.bechat.service;

import id.ac.ui.cs.advprog.bechat.dto.SendMessageRequest;
import id.ac.ui.cs.advprog.bechat.model.ChatMessage;
import id.ac.ui.cs.advprog.bechat.model.ChatSession;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ChatService {
    CompletableFuture<ChatMessage> sendMessage(SendMessageRequest request, UUID senderId);
    CompletableFuture<List<ChatMessage>> getMessages(UUID sessionId, UUID userId);
    CompletableFuture<ChatMessage> editMessage(UUID messageId, String newContent, UUID userId);
    CompletableFuture<ChatMessage> deleteMessage(UUID messageId, UUID userId);
    ChatSession getSessionById(UUID sessionId);
    CompletableFuture<Optional<ChatSession>> findSessionById(UUID sessionId);
}
