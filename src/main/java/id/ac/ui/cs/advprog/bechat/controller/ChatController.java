package id.ac.ui.cs.advprog.bechat.controller;

import id.ac.ui.cs.advprog.bechat.dto.EditMessageRequest;
import id.ac.ui.cs.advprog.bechat.dto.SendMessageRequest;
import id.ac.ui.cs.advprog.bechat.model.ChatMessage;
import id.ac.ui.cs.advprog.bechat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/send")
    public ResponseEntity<ChatMessage> sendMessage(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody SendMessageRequest dto
    ) {
        String token = authHeader.replace("Bearer ", "");
        ChatMessage saved = chatService.sendMessage(dto, token);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/session/{id}")
    public ResponseEntity<List<ChatMessage>> getMessages(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id
    ) {
        String token = authHeader.replace("Bearer ", "");
        List<ChatMessage> messages = chatService.getMessages(id, token);
        return ResponseEntity.ok(messages);
    }

    @PutMapping("/message/{id}")
    public ResponseEntity<ChatMessage> editMessage(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id,
            @Valid @RequestBody EditMessageRequest request
    ) {
        String token = authHeader.replace("Bearer ", "");
        ChatMessage updated = chatService.editMessage(id, request.getContent(), token);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/message/{id}")
    public ResponseEntity<ChatMessage> deleteMessage(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id
    ) {
        String token = authHeader.replace("Bearer ", "");
        ChatMessage deleted = chatService.deleteMessage(id, token);
        return ResponseEntity.ok(deleted);
    }
}
