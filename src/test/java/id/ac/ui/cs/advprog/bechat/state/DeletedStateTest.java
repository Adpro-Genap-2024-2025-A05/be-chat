package id.ac.ui.cs.advprog.bechat.state;

import id.ac.ui.cs.advprog.bechat.model.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DeletedStateTest {

    private DeletedState deletedState;
    private ChatMessage message;

    @BeforeEach
    void setUp() {
        deletedState = new DeletedState();
        message = new ChatMessage();
        message.setId(UUID.randomUUID());
        message.setContent("This is deleted");
        message.setDeleted(true);
        message.setState(deletedState); 
    }

    @Test
    void testEdit_shouldThrowException() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            deletedState.edit(message, "Should fail");
        });
        assertEquals("Pesan telah dihapus dan tidak bisa diedit.", exception.getMessage());
    }

    @Test
    void testDelete_shouldThrowException() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            deletedState.delete(message);
        });
        assertEquals("Pesan sudah dihapus.", exception.getMessage());
    }
}
