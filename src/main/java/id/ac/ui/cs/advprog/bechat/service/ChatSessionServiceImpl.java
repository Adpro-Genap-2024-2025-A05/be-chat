package id.ac.ui.cs.advprog.bechat.service;

import id.ac.ui.cs.advprog.bechat.model.ChatSession;
import id.ac.ui.cs.advprog.bechat.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatSessionServiceImpl implements ChatSessionService {

    private final ChatSessionRepository chatSessionRepository;

    @Override
    public ChatSession createSession(UUID user1Id, UUID user2Id) {
        ChatSession session = new ChatSession();
        session.setId(UUID.randomUUID());
        session.setUser1Id(user1Id);
        session.setUser2Id(user2Id);
        session.setCreatedAt(LocalDateTime.now());
        return chatSessionRepository.save(session);
    }

    @Override
    public Optional<ChatSession> findSession(UUID user1Id, UUID user2Id) {
        return chatSessionRepository.findAll().stream()
                .filter(s -> (s.getUser1Id().equals(user1Id) && s.getUser2Id().equals(user2Id)) ||
                        (s.getUser1Id().equals(user2Id) && s.getUser2Id().equals(user1Id)))
                .findFirst();
    }

    @Override
    public List<ChatSession> getSessionsByUser(UUID userId) {
        return chatSessionRepository.findByUser1IdOrUser2Id(userId, userId);
    }

    @Override
    public void deleteSession(UUID sessionId) {
        chatSessionRepository.deleteById(sessionId);
}
}
