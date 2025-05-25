package id.ac.ui.cs.advprog.bechat.state;

import id.ac.ui.cs.advprog.bechat.model.ChatMessage;

public interface MessageState {
    void edit(ChatMessage message, String newContent);
    void delete(ChatMessage message);
}
