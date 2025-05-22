package id.ac.ui.cs.advprog.bechat.state;

import id.ac.ui.cs.advprog.bechat.model.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EditedStateTest {

    private EditedState editedState;
    private ChatMessage message;

    @BeforeEach
    void setUp() {
        editedState = new EditedState();
        message = new ChatMessage();
        message.setId(UUID.randomUUID());
        message.setContent("Original");
        message.setDeleted(false);
        message.setEdited(true); 
    }

    @Test
    void testEdit_shouldUpdateContent() {
        editedState.edit(message, "Updated content");
        assertEquals("Updated content", message.getContent());
    }

    @Test
    void testDelete_shouldUpdateContentAndState() {
        editedState.delete(message);
        assertEquals("Pesan telah dihapus", message.getContent());
        assertTrue(message.isDeleted());
        assertInstanceOf(DeletedState.class, message.getState());
    }
}
