package id.ac.ui.cs.advprog.bechat.service;
import lombok.RequiredArgsConstructor;
import id.ac.ui.cs.advprog.bechat.model.ChatMessage;
import id.ac.ui.cs.advprog.bechat.model.ChatSession;
import id.ac.ui.cs.advprog.bechat.repository.ChatMessageRepository;
import id.ac.ui.cs.advprog.bechat.repository.ChatSessionRepository;
import id.ac.ui.cs.advprog.bechat.strategy.MessageActionStrategy;
import id.ac.ui.cs.advprog.bechat.strategy.EditMessageStrategy;
import id.ac.ui.cs.advprog.bechat.strategy.DeleteMessageStrategy;
import id.ac.ui.cs.advprog.bechat.dto.SendMessageRequest;
import lombok.RequiredArgsConstructor;
import java.util.UUID;
import java.util.List;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@RequiredArgsConstructor
@Service
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatSessionRepository chatSessionRepository;

    @Override
    public ChatMessage sendMessage(SendMessageRequest dto) {
        ChatSession session = chatSessionRepository.findById(dto.getSessionId())
                .orElseThrow(() -> new RuntimeException("Session not found"));

        ChatMessage message = new ChatMessage();
        message.setId(UUID.randomUUID());
        message.setSession(session);
        message.setSenderId(dto.getSenderId());
        message.setContent(dto.getContent());
        message.setCreatedAt(LocalDateTime.now());
        message.setEdited(false);
        message.setDeleted(false);

        return chatMessageRepository.save(message);
    }

    @Override
    public List<ChatMessage> getMessages(UUID sessionId) {
        return chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    @Override
    public ChatMessage editMessage(UUID messageId, String newContent) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        MessageActionStrategy strategy = new EditMessageStrategy();
        strategy.process(message, newContent);

        return chatMessageRepository.save(message);
    }

    @Override
    public ChatMessage deleteMessage(UUID messageId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        MessageActionStrategy strategy = new DeleteMessageStrategy();
        strategy.process(message, null);

        return chatMessageRepository.save(message);
    }
}
