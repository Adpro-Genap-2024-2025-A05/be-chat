package id.ac.ui.cs.advprog.bechat.dto;

import id.ac.ui.cs.advprog.bechat.model.ChatMessage;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatSessionWithMessagesDto {
    private UUID sessionId;
    private UUID pacilian;
    private String pacilianName;
    private UUID caregiver;
    private String caregiverName;
    private List<ChatMessage> messages;
}
