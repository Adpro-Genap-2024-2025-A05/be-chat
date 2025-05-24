package id.ac.ui.cs.advprog.bechat.service;

import id.ac.ui.cs.advprog.bechat.dto.TokenVerificationResponseDto;
import id.ac.ui.cs.advprog.bechat.model.ChatSession;
import id.ac.ui.cs.advprog.bechat.model.enums.Role;
import id.ac.ui.cs.advprog.bechat.repository.ChatSessionRepository;
import io.micrometer.core.instrument.Counter;
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
    private final CaregiverInfoService caregiverInfoService;

    private final Counter chatSessionCreatedCounter;
    private final Counter chatSessionCreateFailureCounter;

    @Override
    public ChatSession createSession(UUID pacilian, UUID caregiver, String token) {
        try {
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

            TokenVerificationResponseDto pacilianInfo = tokenVerificationService.verifyToken(token);
            String pacilianName = pacilianInfo.getName();

            String caregiverName = caregiverInfoService.getNameByUserIdCaregiver(caregiver, token);

            ChatSession session = findSession(pacilian, caregiver)
                    .orElseGet(() -> {
                        ChatSession newSession = new ChatSession();
                        newSession.setId(UUID.randomUUID());
                        newSession.setPacilian(pacilian);
                        newSession.setCaregiver(caregiver);
                        newSession.setPacilianName(pacilianName);
                        newSession.setCaregiverName(caregiverName);
                        return chatSessionRepository.save(newSession);
                    });

            chatSessionCreatedCounter.increment();
            return session;
        } catch (Exception e) {
            chatSessionCreateFailureCounter.increment();
            throw e;
        }
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
