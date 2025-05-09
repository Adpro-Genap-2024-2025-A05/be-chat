package id.ac.ui.cs.advprog.bechat.controller;

import id.ac.ui.cs.advprog.bechat.dto.*;
import id.ac.ui.cs.advprog.bechat.model.ChatSession;
import id.ac.ui.cs.advprog.bechat.service.ChatSessionService;
import id.ac.ui.cs.advprog.bechat.service.TokenVerificationService;
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
            @RequestHeader("Authorization") String authHeader
    ) {
        UUID user1Id = tokenService.getUserIdFromToken(extractToken(authHeader));
        ChatSession session = chatSessionService.createSession(user1Id, request.getUser2Id());
        return ResponseEntity.ok(session);
    }

    @GetMapping("/user")
    public ResponseEntity<List<ChatSession>> getSessionsForCurrentUser(
            @RequestHeader("Authorization") String authHeader
    ) {
        UUID userId = tokenService.getUserIdFromToken(extractToken(authHeader));
        return ResponseEntity.ok(chatSessionService.getSessionsByUser(userId));
    }


    private String extractToken(String header) {
        return header.replace("Bearer ", "");
    }
}
