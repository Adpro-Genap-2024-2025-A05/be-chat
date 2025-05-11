package id.ac.ui.cs.advprog.bechat.controller;

import id.ac.ui.cs.advprog.bechat.dto.CreateSessionRequest;
import id.ac.ui.cs.advprog.bechat.dto.TokenVerificationResponseDto;
import id.ac.ui.cs.advprog.bechat.model.ChatSession;
import id.ac.ui.cs.advprog.bechat.service.ChatSessionService;
import id.ac.ui.cs.advprog.bechat.service.TokenVerificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat/session")
public class ChatSessionController {

    private final ChatSessionService chatSessionService;
    private final TokenVerificationService tokenService;

    @PostMapping("/create")
    public ResponseEntity<ChatSession> createSession(
            @Valid @RequestBody CreateSessionRequest request,
            HttpServletRequest httpRequest
    ) {
        UUID userId = getUserIdFromRequest(httpRequest);
        ChatSession session = chatSessionService.createSession(userId, request.getCaregiver());
        return ResponseEntity.ok(session);
    }

    @GetMapping("/user")
    public ResponseEntity<List<ChatSession>> getSessionsForCurrentUser(HttpServletRequest httpRequest) {
        UUID userId = getUserIdFromRequest(httpRequest);
        return ResponseEntity.ok(chatSessionService.getSessionsByUser(userId));
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization header is missing or invalid");
        }
        return header.substring(7);
    }

    private UUID getUserIdFromRequest(HttpServletRequest request) {
        String token = extractToken(request);
        TokenVerificationResponseDto verification = tokenService.verifyToken(token);
        return UUID.fromString(verification.getUserId());
    }
}
