package id.ac.ui.cs.advprog.bechat.service;

import id.ac.ui.cs.advprog.bechat.dto.SendMessageRequest;
import id.ac.ui.cs.advprog.bechat.model.ChatMessage;
import id.ac.ui.cs.advprog.bechat.model.ChatSession;

import java.util.List;
import java.util.UUID;

public interface ChatService {
    ChatMessage sendMessage(SendMessageRequest dto, String token);
    List<ChatMessage> getMessages(UUID sessionId, String token);
    ChatMessage editMessage(UUID messageId, String newContent, String token);
    ChatMessage deleteMessage(UUID messageId, String token);
    ChatSession getSessionById(UUID sessionId);
}
