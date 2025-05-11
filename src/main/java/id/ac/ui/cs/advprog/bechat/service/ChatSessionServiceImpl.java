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
    public ChatSession createSession(UUID pacilian, UUID caregiver) {
        if (pacilian == null || caregiver == null) {
            throw new IllegalArgumentException("Pacilian and Caregiver must not be null");
        }

        return findSession(pacilian, caregiver)
                .orElseGet(() -> {
                    ChatSession session = new ChatSession();
                    session.setId(UUID.randomUUID());
                    session.setPacilian(pacilian);
                    session.setCaregiver(caregiver);
                    session.setCreatedAt(LocalDateTime.now());
                    return chatSessionRepository.save(session);
                });
    }

    @Override
    public Optional<ChatSession> findSession(UUID pacilian, UUID caregiver) {
        return chatSessionRepository.findAll().stream()
            .filter(s -> s.getPacilian() != null && s.getCaregiver() != null)
            .filter(s ->
                (s.getPacilian().equals(pacilian) && s.getCaregiver().equals(caregiver)) ||
                (s.getPacilian().equals(caregiver) && s.getCaregiver().equals(pacilian))
            )
            .findFirst();
    }

    @Override
    public List<ChatSession> getSessionsByUser(UUID userId) {
        return chatSessionRepository.findByPacilianOrCaregiver(userId, userId);
    }
}