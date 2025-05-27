package id.ac.ui.cs.advprog.bechat.service;

import id.ac.ui.cs.advprog.bechat.dto.TokenVerificationResponseDto;
import id.ac.ui.cs.advprog.bechat.model.ChatSession;
import id.ac.ui.cs.advprog.bechat.model.enums.Role;
import id.ac.ui.cs.advprog.bechat.repository.ChatSessionRepository;
import io.micrometer.core.instrument.Counter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ChatSessionServiceImplTest {

    private ChatSessionRepository chatSessionRepository;
    private TokenVerificationService tokenVerificationService;
    private CaregiverInfoService caregiverInfoService;
    private Counter chatSessionCreatedCounter;
    private Counter chatSessionCreateFailureCounter;

    private ChatSessionServiceImpl chatSessionService;

    private static final String TOKEN = "faketoken";

    @BeforeEach
    void setUp() {
        chatSessionRepository = mock(ChatSessionRepository.class);
        tokenVerificationService = mock(TokenVerificationService.class);
        caregiverInfoService = mock(CaregiverInfoService.class);
        chatSessionCreatedCounter = mock(Counter.class);
        chatSessionCreateFailureCounter = mock(Counter.class);

        chatSessionService = new ChatSessionServiceImpl(
            chatSessionRepository,
            tokenVerificationService,
            caregiverInfoService,
            chatSessionCreatedCounter,
            chatSessionCreateFailureCounter
        );
    }

    @Test
    void testCreateSession_shouldReturnSavedSession() {
        UUID pacilianId = UUID.randomUUID();
        UUID caregiverId = UUID.randomUUID();

        when(tokenVerificationService.getRoleFromToken(TOKEN)).thenReturn(Role.PACILIAN);
        when(tokenVerificationService.verifyToken(TOKEN)).thenReturn(
            TokenVerificationResponseDto.builder()
                .userId(pacilianId.toString())
                .name("Cleo")
                .role(Role.PACILIAN)
                .email("cleo@mail.com")
                .valid(true)
                .build()
        );
        when(caregiverInfoService.getNameByUserIdCaregiver(caregiverId, TOKEN)).thenReturn("Dr. Panda");
        when(chatSessionRepository.findAll()).thenReturn(Collections.emptyList());
        when(chatSessionRepository.save(any(ChatSession.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        ChatSession session = chatSessionService.createSession(pacilianId, caregiverId, TOKEN);

        assertEquals(pacilianId, session.getPacilian());
        assertEquals(caregiverId, session.getCaregiver());
        assertEquals("Cleo", session.getPacilianName());
        assertEquals("Dr. Panda", session.getCaregiverName());
        assertNotNull(session.getId());

        verify(chatSessionCreatedCounter).increment();
        verify(chatSessionCreateFailureCounter, never()).increment();
    }

    @Test
    void testCreateSession_shouldThrowExceptionIfPacilianOrCaregiverIsNull() {
        UUID caregiverId = UUID.randomUUID();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> chatSessionService.createSession(null, caregiverId, TOKEN));

        assertEquals("Pacilian and Caregiver must not be null", exception.getMessage());
        verify(chatSessionCreateFailureCounter).increment();
    }

    @Test
    void testCreateSession_shouldThrowExceptionIfPacilianEqualsCaregiver() {
        UUID sameId = UUID.randomUUID();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> chatSessionService.createSession(sameId, sameId, TOKEN));

        assertEquals("Pacilian tidak boleh membuat sesi dengan dirinya sendiri", exception.getMessage());
        verify(chatSessionCreateFailureCounter).increment();
    }

    @Test
    void testCreateSession_shouldThrowExceptionIfRoleIsNotPacilian() {
        UUID pacilianId = UUID.randomUUID();
        UUID caregiverId = UUID.randomUUID();

        when(tokenVerificationService.getRoleFromToken(TOKEN)).thenReturn(Role.CAREGIVER);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> chatSessionService.createSession(pacilianId, caregiverId, TOKEN));

        assertEquals("Only PACILIAN can create session", exception.getMessage());
        verify(chatSessionCreateFailureCounter).increment();
    }

    @Test
    void testCreateSession_shouldReturnExistingSessionIfAlreadyExists() {
        UUID pacilianId = UUID.randomUUID();
        UUID caregiverId = UUID.randomUUID();

        ChatSession existingSession = new ChatSession();
        existingSession.setId(UUID.randomUUID());
        existingSession.setPacilian(pacilianId);
        existingSession.setCaregiver(caregiverId);
        existingSession.setPacilianName("Cleo");
        existingSession.setCaregiverName("Dr. Panda");

        when(tokenVerificationService.getRoleFromToken(TOKEN)).thenReturn(Role.PACILIAN);
        when(tokenVerificationService.verifyToken(TOKEN)).thenReturn(
            TokenVerificationResponseDto.builder().name("Cleo").build()
        );
        when(caregiverInfoService.getNameByUserIdCaregiver(caregiverId, TOKEN)).thenReturn("Dr. Panda");
        when(chatSessionRepository.findAll()).thenReturn(List.of(existingSession));

        ChatSession session = chatSessionService.createSession(pacilianId, caregiverId, TOKEN);

        assertEquals(existingSession, session);
        verify(chatSessionCreatedCounter).increment();
    }

    @Test
    void testFindSession_shouldReturnIfFound() {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();

        ChatSession session = new ChatSession();
        session.setPacilian(user1);
        session.setCaregiver(user2);

        when(chatSessionRepository.findAll()).thenReturn(List.of(session));

        Optional<ChatSession> result = chatSessionService.findSession(user1, user2);

        assertTrue(result.isPresent());
        assertEquals(session, result.get());
    }

    @Test
    void testFindSession_shouldReturnIfUsersAreSwapped() {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();

        ChatSession session = new ChatSession();
        session.setPacilian(user2);
        session.setCaregiver(user1);

        when(chatSessionRepository.findAll()).thenReturn(List.of(session));

        Optional<ChatSession> result = chatSessionService.findSession(user1, user2);

        assertTrue(result.isPresent());
        assertEquals(session, result.get());
    }

    @Test
    void testFindSession_shouldReturnEmptyIfNotFound() {
        when(chatSessionRepository.findAll()).thenReturn(Collections.emptyList());

        Optional<ChatSession> result = chatSessionService.findSession(UUID.randomUUID(), UUID.randomUUID());

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetSessionsByUser_shouldReturnSessions() {
        UUID userId = UUID.randomUUID();
        ChatSession session = new ChatSession();
        session.setPacilian(userId);
        session.setCaregiver(UUID.randomUUID());

        when(chatSessionRepository.findByPacilianOrCaregiver(userId, userId)).thenReturn(List.of(session));

        List<ChatSession> result = chatSessionService.getSessionsByUser(userId);

        assertEquals(1, result.size());
        assertEquals(session, result.get(0));
    }
}
