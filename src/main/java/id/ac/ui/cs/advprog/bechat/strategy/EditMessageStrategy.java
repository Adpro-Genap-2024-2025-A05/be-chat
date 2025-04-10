package id.ac.ui.cs.advprog.bechat.strategy;
import id.ac.ui.cs.advprog.bechat.model.builder.ChatMessage;
import java.time.LocalDateTime;

public class EditMessageStrategy implements MessageActionStrategy {
    @Override
    public void process(ChatMessage message, String newContent) {
        message.setContent(newContent);
        message.setEdited(true);
        message.setEditedAt(LocalDateTime.now());
    }
}
