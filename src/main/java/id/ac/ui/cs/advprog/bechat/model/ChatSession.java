package id.ac.ui.cs.advprog.bechat.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class ChatSession {

    @Id
    private UUID id;

    private UUID user1Id;
    private UUID user2Id;
    private LocalDateTime createdAt;
}
