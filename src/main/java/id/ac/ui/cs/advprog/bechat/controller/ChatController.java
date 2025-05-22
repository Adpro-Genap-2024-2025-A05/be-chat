package id.ac.ui.cs.advprog.bechat.controller;

import id.ac.ui.cs.advprog.bechat.dto.*;
import id.ac.ui.cs.advprog.bechat.model.ChatMessage;
import id.ac.ui.cs.advprog.bechat.model.ChatSession;
import id.ac.ui.cs.advprog.bechat.service.ChatService;
import id.ac.ui.cs.advprog.bechat.service.TokenVerificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;
    private final TokenVerificationService tokenVerificationService;

    @PostMapping("/send")
    public ResponseEntity<BaseResponseDTO<ChatMessage>> sendMessage(
            @Valid @RequestBody SendMessageRequest dto,
            HttpServletRequest request
    ) {
        UUID userId = getUserIdFromRequest(request);
        ChatMessage saved = chatService.sendMessage(dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponseDTO.success(HttpStatus.CREATED.value(), "Message sent successfully", saved));
    }

    @GetMapping("/session/{id}")
    public ResponseEntity<BaseResponseDTO<ChatSessionWithMessagesDto>> getMessages(
            @PathVariable UUID id,
            HttpServletRequest request
    ) {
        UUID userId = getUserIdFromRequest(request);
        List<ChatMessage> messages = chatService.getMessages(id, userId);

        ChatSession session = messages.isEmpty()
                ? chatService.findSessionById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"))
                : messages.get(0).getSession();

        var responseDto = ChatSessionWithMessagesDto.builder()
                .sessionId(session.getId())
                .pacilian(session.getPacilian())
                .pacilianName(session.getPacilianName())
                .caregiver(session.getCaregiver())
                .caregiverName(session.getCaregiverName())
                .messages(messages)
                .build();

        return ResponseEntity.ok(BaseResponseDTO.success(
                HttpStatus.OK.value(),
                "Messages retrieved",
                responseDto));
    }

    @PutMapping("/message/{id}")
    public ResponseEntity<BaseResponseDTO<ChatMessage>> editMessage(
            @PathVariable UUID id,
            @Valid @RequestBody EditMessageRequest requestBody,
            HttpServletRequest request
    ) {
        UUID userId = getUserIdFromRequest(request);
        ChatMessage updated = chatService.editMessage(id, requestBody.getContent(), userId);
        return ResponseEntity.ok(BaseResponseDTO.success(HttpStatus.OK.value(), "Message updated", updated));
    }

    @DeleteMapping("/message/{id}")
    public ResponseEntity<BaseResponseDTO<ChatMessage>> deleteMessage(
            @PathVariable UUID id,
            HttpServletRequest request
    ) {
        UUID userId = getUserIdFromRequest(request);
        ChatMessage deleted = chatService.deleteMessage(id, userId);
        return ResponseEntity.ok(BaseResponseDTO.success(HttpStatus.OK.value(), "Message deleted", deleted));
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
