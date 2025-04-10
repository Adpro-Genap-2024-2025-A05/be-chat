package id.ac.ui.cs.advprog.bechat.service;

import id.ac.ui.cs.advprog.bechat.model.builder.ChatSession;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatSessionService {
    ChatSession createSession(UUID user1Id, UUID user2Id);
    Optional<ChatSession> findSession(UUID user1Id, UUID user2Id);
    List<ChatSession> getSessionsByUser(UUID userId);
}

