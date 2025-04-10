package id.ac.ui.cs.advprog.bechat.service;

import id.ac.ui.cs.advprog.bechat.dto.SendMessageRequest;
import id.ac.ui.cs.advprog.bechat.model.builder.ChatMessage;

import java.util.List;
import java.util.UUID;

public interface ChatService {
    ChatMessage sendMessage(SendMessageRequest dto);
    List<ChatMessage> getMessages(UUID sessionId);
    ChatMessage editMessage(UUID messageId, String newContent);
    ChatMessage deleteMessage(UUID messageId);
}
