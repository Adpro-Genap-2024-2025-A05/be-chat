package id.ac.ui.cs.advprog.bechat.service;

import id.ac.ui.cs.advprog.bechat.dto.SendMessageRequest;
import id.ac.ui.cs.advprog.bechat.model.ChatMessage;
import id.ac.ui.cs.advprog.bechat.model.ChatSession;

import java.util.List;
import java.util.UUID;

public interface ChatService {
    ChatMessage sendMessage(SendMessageRequest request, UUID senderId);
    List<ChatMessage> getMessages(UUID sessionId, UUID userId);
    ChatMessage editMessage(UUID messageId, String newContent, UUID userId);
    ChatMessage deleteMessage(UUID messageId, UUID userId);
    ChatSession getSessionById(UUID sessionId);
}
