package id.ac.ui.cs.advprog.bechat.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import id.ac.ui.cs.advprog.bechat.model.ChatSession;

public interface ChatSessionService {
    ChatSession createSession(UUID user1Id, UUID user2Id);
    Optional<ChatSession> findSession(UUID user1Id, UUID user2Id);
    List<ChatSession> getSessionsByUser(UUID userId);
}

