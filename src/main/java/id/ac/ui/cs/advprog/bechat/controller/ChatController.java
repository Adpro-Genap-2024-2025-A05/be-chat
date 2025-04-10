package id.ac.ui.cs.advprog.bechat.controller;

import id.ac.ui.cs.advprog.bechat.dto.EditMessageRequest;
import id.ac.ui.cs.advprog.bechat.dto.SendMessageRequest;
import id.ac.ui.cs.advprog.bechat.model.builder.ChatMessage;
import id.ac.ui.cs.advprog.bechat.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/send")
    public ResponseEntity<ChatMessage> sendMessage(@Valid @RequestBody SendMessageRequest dto) {
        ChatMessage saved = chatService.sendMessage(dto);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/session/{id}")
    public ResponseEntity<List<ChatMessage>> getMessages(@PathVariable UUID id) {
        List<ChatMessage> messages = chatService.getMessages(id);
        return ResponseEntity.ok(messages);
    }

    @PutMapping("/message/{id}")
    public ResponseEntity<ChatMessage> editMessage(
            @PathVariable UUID id,
            @Valid @RequestBody EditMessageRequest request
    ) {
        ChatMessage updated = chatService.editMessage(id, request.getContent());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/message/{id}")
    public ResponseEntity<ChatMessage> deleteMessage(@PathVariable UUID id) {
        ChatMessage deleted = chatService.deleteMessage(id);
        return ResponseEntity.ok(deleted);
    }
}
