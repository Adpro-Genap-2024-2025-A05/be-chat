package id.ac.ui.cs.advprog.bechat.state;

import id.ac.ui.cs.advprog.bechat.model.ChatMessage;

import java.util.Date;

public class NormalState implements MessageState {

    @Override
    public void edit(ChatMessage message, String newContent) {
        message.setContent(newContent);
        message.setEdited(true);
        message.setEditedAt(new Date());
        message.setState(new EditedState());
    }

    @Override
    public void delete(ChatMessage message) {
        message.setContent("Pesan telah dihapus");
        message.setDeleted(true);
        message.setState(new DeletedState());
    }
}
