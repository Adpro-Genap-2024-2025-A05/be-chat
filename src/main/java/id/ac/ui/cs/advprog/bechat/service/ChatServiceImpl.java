package id.ac.ui.cs.advprog.bechat.service;

import id.ac.ui.cs.advprog.bechat.dto.SendMessageRequest;
import id.ac.ui.cs.advprog.bechat.model.ChatMessage;
import id.ac.ui.cs.advprog.bechat.model.ChatSession;
import id.ac.ui.cs.advprog.bechat.repository.ChatMessageRepository;
import id.ac.ui.cs.advprog.bechat.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatSessionRepository chatSessionRepository;

    @Override
    public ChatMessage sendMessage(SendMessageRequest dto, UUID senderId) {
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

        return chatMessageRepository.save(message);
    }

    @Override
    public List<ChatMessage> getMessages(UUID sessionId, UUID userId) {
        ChatSession session = getSessionById(sessionId);

        if (!session.getPacilian().equals(userId) && !session.getCaregiver().equals(userId)) {
            throw new SecurityException("You do not have access to this session.");
        }

        return chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    @Override
    public ChatMessage editMessage(UUID messageId, String newContent, UUID userId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (!message.getSenderId().equals(userId)) {
            throw new SecurityException("You can only edit your own messages.");
        }

        message.edit(newContent);
        return chatMessageRepository.save(message);
    }

    @Override
    public ChatMessage deleteMessage(UUID messageId, UUID userId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (!message.getSenderId().equals(userId)) {
            throw new SecurityException("You can only delete your own messages.");
        }

        message.delete();
        return chatMessageRepository.save(message);
    }

    @Override
    public ChatSession getSessionById(UUID sessionId) {
        return chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
    }
    
    public Optional<ChatSession> findSessionById(UUID sessionId) {
        return chatSessionRepository.findById(sessionId);
    }
}
