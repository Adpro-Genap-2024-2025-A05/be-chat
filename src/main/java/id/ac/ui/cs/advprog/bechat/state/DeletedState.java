package id.ac.ui.cs.advprog.bechat.state;

import id.ac.ui.cs.advprog.bechat.model.ChatMessage;

public class DeletedState implements MessageState {

    @Override
    public void edit(ChatMessage message, String newContent) {
        throw new IllegalStateException("Pesan telah dihapus dan tidak bisa diedit.");
    }

    @Override
    public void delete(ChatMessage message) {
        throw new IllegalStateException("Pesan sudah dihapus.");
    }
}
