package id.ac.ui.cs.advprog.bechat.service;

import id.ac.ui.cs.advprog.bechat.model.ChatSession;
import id.ac.ui.cs.advprog.bechat.model.enums.Role;
import id.ac.ui.cs.advprog.bechat.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatSessionServiceImpl implements ChatSessionService {

    private final ChatSessionRepository chatSessionRepository;
    private final TokenVerificationService tokenVerificationService;

    @Override
    public ChatSession createSession(UUID pacilian, UUID caregiver, String token) {
        if (pacilian == null || caregiver == null) {
            throw new IllegalArgumentException("Pacilian and Caregiver must not be null");
        }

        if (pacilian.equals(caregiver)) {
            throw new IllegalArgumentException("Pacilian tidak boleh membuat sesi dengan dirinya sendiri");
        }
        Role requesterRole = tokenVerificationService.getRoleFromToken(token);

        if (requesterRole != Role.PACILIAN) {
            throw new IllegalArgumentException("Only PACILIAN can create session");
        }

        Role pacilianRole = tokenVerificationService.getRoleFromToken(token);
        Role caregiverRole;

        try {
            caregiverRole = Role.CAREGIVER; 
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to fetch caregiver role");
        }

        if (pacilianRole != Role.PACILIAN || caregiverRole != Role.CAREGIVER) {
            throw new IllegalArgumentException("Only PACILIAN can create session with CAREGIVER");
        }

        return findSession(pacilian, caregiver)
                .orElseGet(() -> {
                    ChatSession session = new ChatSession();
                    session.setId(UUID.randomUUID());
                    session.setPacilian(pacilian);
                    session.setCaregiver(caregiver);
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