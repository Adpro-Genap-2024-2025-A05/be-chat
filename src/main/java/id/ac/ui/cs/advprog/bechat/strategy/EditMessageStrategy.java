package id.ac.ui.cs.advprog.bechat.strategy;
import java.time.LocalDateTime;

import id.ac.ui.cs.advprog.bechat.model.ChatMessage;

public class EditMessageStrategy implements MessageActionStrategy {
    @Override
    public void process(ChatMessage message, String newContent) {
        message.setContent(newContent);
        message.setEdited(true);
        message.setEditedAt(LocalDateTime.now());
    }
}
