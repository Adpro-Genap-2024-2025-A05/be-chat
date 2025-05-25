package id.ac.ui.cs.advprog.bechat.service;

import id.ac.ui.cs.advprog.bechat.dto.TokenVerificationResponseDto;
import id.ac.ui.cs.advprog.bechat.model.ChatSession;
import id.ac.ui.cs.advprog.bechat.model.enums.Role;
import id.ac.ui.cs.advprog.bechat.repository.ChatSessionRepository;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatSessionServiceImpl implements ChatSessionService {

    private static final Logger logger = LoggerFactory.getLogger(ChatSessionServiceImpl.class);

    private final ChatSessionRepository chatSessionRepository;
    private final TokenVerificationService tokenVerificationService;
    private final CaregiverInfoService caregiverInfoService;

    private final Counter chatSessionCreatedCounter;
    private final Counter chatSessionCreateFailureCounter;

    @Override
    public ChatSession createSession(UUID pacilian, UUID caregiver, String token) {
        logger.info("Attempting to create chat session for pacilian: {} and caregiver: {}", pacilian, caregiver);

        try {
            if (pacilian == null || caregiver == null) {
                logger.warn("Pacilian or caregiver UUID is null");
                throw new IllegalArgumentException("Pacilian and Caregiver must not be null");
            }

            if (pacilian.equals(caregiver)) {
                logger.warn("Pacilian tried to create session with self: {}", pacilian);
                throw new IllegalArgumentException("Pacilian tidak boleh membuat sesi dengan dirinya sendiri");
            }

            Role requesterRole = tokenVerificationService.getRoleFromToken(token);
            logger.debug("Requester role extracted from token: {}", requesterRole);

            if (requesterRole != Role.PACILIAN) {
                logger.warn("Unauthorized session creation attempt by non-PACILIAN role: {}", requesterRole);
                throw new IllegalArgumentException("Only PACILIAN can create session");
            }

            TokenVerificationResponseDto pacilianInfo = tokenVerificationService.verifyToken(token);
            String pacilianName = pacilianInfo.getName();
            logger.debug("Pacilian name resolved: {}", pacilianName);

            String caregiverName = caregiverInfoService.getNameByUserIdCaregiver(caregiver, token);
            logger.debug("Caregiver name resolved: {}", caregiverName);

            ChatSession session = findSession(pacilian, caregiver)
                    .orElseGet(() -> {
                        logger.info("No existing session found. Creating new session.");
                        ChatSession newSession = new ChatSession();
                        newSession.setId(UUID.randomUUID());
                        newSession.setPacilian(pacilian);
                        newSession.setCaregiver(caregiver);
                        newSession.setPacilianName(pacilianName);
                        newSession.setCaregiverName(caregiverName);
                        return chatSessionRepository.save(newSession);
                    });

            chatSessionCreatedCounter.increment();
            logger.info("Chat session successfully created with ID: {}", session.getId());
            return session;

        } catch (Exception e) {
            chatSessionCreateFailureCounter.increment();
            logger.error("Failed to create chat session for pacilian {} and caregiver {}. Error: {}", pacilian, caregiver, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Optional<ChatSession> findSession(UUID pacilian, UUID caregiver) {
        logger.debug("Searching for existing session between pacilian: {} and caregiver: {}", pacilian, caregiver);
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
        logger.info("Fetching all sessions for user ID: {}", userId);
        List<ChatSession> sessions = chatSessionRepository.findByPacilianOrCaregiver(userId, userId);
        logger.debug("Found {} sessions for user {}", sessions.size(), userId);
        return sessions;
    }
}
