package id.ac.ui.cs.advprog.bechat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import id.ac.ui.cs.advprog.bechat.model.ChatSession;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatSessionRepository extends JpaRepository<ChatSession, UUID> {
    Optional<ChatSession> findByPacilianAndCaregiver(UUID pacilian, UUID caregiver);
    List<ChatSession> findByPacilianOrCaregiver(UUID pacilian, UUID caregiver);
}

