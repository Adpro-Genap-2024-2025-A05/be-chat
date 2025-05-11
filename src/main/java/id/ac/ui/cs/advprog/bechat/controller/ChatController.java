package id.ac.ui.cs.advprog.bechat.controller;

import id.ac.ui.cs.advprog.bechat.dto.EditMessageRequest;
import id.ac.ui.cs.advprog.bechat.dto.SendMessageRequest;
import id.ac.ui.cs.advprog.bechat.dto.TokenVerificationResponseDto;
import id.ac.ui.cs.advprog.bechat.model.ChatMessage;
import id.ac.ui.cs.advprog.bechat.service.ChatService;
import id.ac.ui.cs.advprog.bechat.service.TokenVerificationService;
import jakarta.servlet.http.HttpServletRequest;
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
    private final TokenVerificationService tokenVerificationService;

    @PostMapping("/send")
    public ResponseEntity<ChatMessage> sendMessage(
            @Valid @RequestBody SendMessageRequest dto,
            HttpServletRequest request
    ) {
        UUID userId = getUserIdFromRequest(request);
        ChatMessage saved = chatService.sendMessage(dto, userId);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/session/{id}")
    public ResponseEntity<List<ChatMessage>> getMessages(
            @PathVariable UUID id,
            HttpServletRequest request
    ) {
        UUID userId = getUserIdFromRequest(request);
        List<ChatMessage> messages = chatService.getMessages(id, userId);
        return ResponseEntity.ok(messages);
    }

    @PutMapping("/message/{id}")
    public ResponseEntity<ChatMessage> editMessage(
            @PathVariable UUID id,
            @Valid @RequestBody EditMessageRequest requestBody,
            HttpServletRequest request
    ) {
        UUID userId = getUserIdFromRequest(request);
        ChatMessage updated = chatService.editMessage(id, requestBody.getContent(), userId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/message/{id}")
    public ResponseEntity<ChatMessage> deleteMessage(
            @PathVariable UUID id,
            HttpServletRequest request
    ) {
        UUID userId = getUserIdFromRequest(request);
        ChatMessage deleted = chatService.deleteMessage(id, userId);
        return ResponseEntity.ok(deleted);
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization header is missing or invalid");
        }
        return authHeader.substring(7);
    }

    private UUID getUserIdFromRequest(HttpServletRequest request) {
        String token = extractToken(request);
        TokenVerificationResponseDto verification = tokenVerificationService.verifyToken(token);
        return UUID.fromString(verification.getUserId());
    }
}
