package id.ac.ui.cs.advprog.bechat.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "chat_message")
public class ChatMessage {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    @JsonBackReference
    private ChatSession session;

    private UUID senderId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime editedAt;
    private boolean edited;
    private boolean deleted;
}

