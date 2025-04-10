package id.ac.ui.cs.advprog.bechat.strategy;

import id.ac.ui.cs.advprog.bechat.model.builder.ChatMessage;

public interface MessageActionStrategy {
    void process(ChatMessage message, String newContent);
}
