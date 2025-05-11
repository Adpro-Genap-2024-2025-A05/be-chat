package id.ac.ui.cs.advprog.bechat.service;

import id.ac.ui.cs.advprog.bechat.model.ChatSession;
import id.ac.ui.cs.advprog.bechat.repository.ChatSessionRepository;
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
    private ChatSessionService chatSessionService;

    @BeforeEach
    void setUp() {
        chatSessionRepository = mock(ChatSessionRepository.class);
        chatSessionService = new ChatSessionServiceImpl(chatSessionRepository);
    }

    @Test
    void testCreateSession_shouldReturnSavedSession() {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();

        when(chatSessionRepository.save(any(ChatSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ChatSession session = chatSessionService.createSession(user1, user2);

        assertEquals(user1, session.getPacilian());
        assertEquals(user2, session.getCaregiver());
        assertNotNull(session.getId());
        assertNotNull(session.getCreatedAt());
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
}
