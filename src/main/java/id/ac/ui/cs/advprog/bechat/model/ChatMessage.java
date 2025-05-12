package id.ac.ui.cs.advprog.bechat.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import id.ac.ui.cs.advprog.bechat.state.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "chat_message")
public class ChatMessage {

    @Id
    @NotNull
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    @JsonBackReference
    @NotNull
    private ChatSession session;

    @NotNull
    @Column(nullable = false)
    private UUID senderId;

    @NotNull
    @Column(nullable = false)
    private String content;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date editedAt;

    @Column(nullable = false)
    private boolean edited;

    @Column(nullable = false)
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
