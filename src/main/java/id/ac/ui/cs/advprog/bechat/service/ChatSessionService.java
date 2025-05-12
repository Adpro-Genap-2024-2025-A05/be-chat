package id.ac.ui.cs.advprog.bechat.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import id.ac.ui.cs.advprog.bechat.model.ChatSession;

public interface ChatSessionService {
    public ChatSession createSession(UUID pacilian, UUID caregiver, String token);
    Optional<ChatSession> findSession(UUID pacilian, UUID caregiver);
    List<ChatSession> getSessionsByUser(UUID userId);
}
