package id.ac.ui.cs.advprog.bechat.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import id.ac.ui.cs.advprog.bechat.state.*;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

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


    @Transient
    @JsonIgnore
    private MessageState state = new NormalState();

    @PostLoad
    public void initState() {
        if (deleted) {
            state = new DeletedState();
        } else if (edited) {
            state = new EditedState();
        } else {
            state = new NormalState();
        }
    }

    public void edit(String newContent) {
        state.edit(this, newContent);
    }

    public void delete() {
        state.delete(this);
    }

    public void setState(MessageState state) {
        this.state = state;
    }
}
