package id.ac.ui.cs.advprog.bechat.controller;

import id.ac.ui.cs.advprog.bechat.dto.CreateSessionRequest;
import id.ac.ui.cs.advprog.bechat.model.builder.ChatSession;
import id.ac.ui.cs.advprog.bechat.service.ChatSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat/session")
public class ChatSessionController {

    private final ChatSessionService chatSessionService;

    @GetMapping("/find")
    public ResponseEntity<ChatSession> findSession(@RequestParam UUID user1, @RequestParam UUID user2) {
        Optional<ChatSession> sessionOpt = chatSessionService.findSession(user1, user2);
        return sessionOpt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/create")
    public ResponseEntity<ChatSession> createSession(@Valid @RequestBody CreateSessionRequest request) {
        ChatSession session = chatSessionService.createSession(request.getUser1Id(), request.getUser2Id());
        return ResponseEntity.ok(session);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ChatSession>> getSessionsByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(chatSessionService.getSessionsByUser(userId));
    }
}
