package id.ac.ui.cs.advprog.bechat.model;

import id.ac.ui.cs.advprog.bechat.state.DeletedState;
import id.ac.ui.cs.advprog.bechat.state.EditedState;
import id.ac.ui.cs.advprog.bechat.state.NormalState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ChatMessageTest {

    private ChatMessage message;
    private ChatSession session;

    @BeforeEach
    void setUp() {
        session = new ChatSession();
        message = new ChatMessage();
        message.setId(UUID.randomUUID());
        message.setSession(session);
        message.setSenderId(UUID.randomUUID());
        message.setContent("Hello");
        message.setCreatedAt(new Date());
        message.setEdited(false);
        message.setDeleted(false);
        message.initState(); 
    }

    @Test
    void testInitState_shouldSetNormalState() {
        message.setEdited(false);
        message.setDeleted(false);
        message.initState();

        assertInstanceOf(NormalState.class, message.getState());
    }

    @Test
    void testInitState_shouldSetEditedState() {
        message.setEdited(true);
        message.setDeleted(false);
        message.initState();

        assertInstanceOf(EditedState.class, message.getState());
    }

    @Test
    void testInitState_shouldSetDeletedState() {
        message.setEdited(true); 
        message.setDeleted(true);
        message.initState();

        assertInstanceOf(DeletedState.class, message.getState());
    }

    @Test
    void testEdit_shouldChangeContentAndSetEditedTrue() {
        message.edit("New Content");

        assertEquals("New Content", message.getContent());
        assertTrue(message.isEdited());
        assertInstanceOf(EditedState.class, message.getState());
    }

    @Test
    void testDelete_shouldMarkAsDeletedAndSetState() {
        message.delete();

        assertTrue(message.isDeleted());
        assertInstanceOf(DeletedState.class, message.getState());
    }
}
