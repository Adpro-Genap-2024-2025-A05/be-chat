package id.ac.ui.cs.advprog.bechat.strategy;
import id.ac.ui.cs.advprog.bechat.model.ChatMessage;

public class DeleteMessageStrategy implements MessageActionStrategy {
    @Override
    public void process(ChatMessage message, String ignored) {
        message.setContent("Pesan telah dihapus");
        message.setDeleted(true);
    }
}
