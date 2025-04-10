package id.ac.ui.cs.advprog.bechat.repository;

import id.ac.ui.cs.advprog.bechat.model.builder.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatSessionRepository extends JpaRepository<ChatSession, UUID> {
    Optional<ChatSession> findByUser1IdAndUser2Id(UUID user1Id, UUID user2Id);
    List<ChatSession> findByUser1IdOrUser2Id(UUID user1Id, UUID user2Id);
}

