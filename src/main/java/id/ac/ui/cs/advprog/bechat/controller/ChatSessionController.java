package id.ac.ui.cs.advprog.bechat.controller;

import id.ac.ui.cs.advprog.bechat.dto.BaseResponseDTO;
import id.ac.ui.cs.advprog.bechat.dto.CreateSessionRequest;
import id.ac.ui.cs.advprog.bechat.dto.TokenVerificationResponseDto;
import id.ac.ui.cs.advprog.bechat.model.ChatSession;
import id.ac.ui.cs.advprog.bechat.service.ChatSessionService;
import id.ac.ui.cs.advprog.bechat.service.TokenVerificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = "*")
@RestController
@RequiredArgsConstructor
@RequestMapping("/chat/session")
public class ChatSessionController {

    private final ChatSessionService chatSessionService;
    private final TokenVerificationService tokenService;

    @PostMapping("/create")
    public ResponseEntity<BaseResponseDTO<ChatSession>> createSession(
            @Valid @RequestBody CreateSessionRequest request,
            HttpServletRequest httpRequest
    ) {
        String token = extractToken(httpRequest);
        UUID userId = getUserIdFromRequest(httpRequest);
        ChatSession session = chatSessionService.createSession(userId, request.getCaregiver(), token);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponseDTO.success(HttpStatus.CREATED.value(), "Session created successfully", session));
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/user")
    public ResponseEntity<BaseResponseDTO<List<ChatSession>>> getSessionsForCurrentUser(HttpServletRequest httpRequest) {
        UUID userId = getUserIdFromRequest(httpRequest);
        List<ChatSession> sessions = chatSessionService.getSessionsByUser(userId);
        return ResponseEntity.ok(BaseResponseDTO.success(HttpStatus.OK.value(), "Sessions retrieved", sessions));
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization header is missing or invalid");
        }
        return header.substring(7);
    }

    private UUID getUserIdFromToken(String token) {
        TokenVerificationResponseDto verification = tokenService.verifyToken(token);
        return UUID.fromString(verification.getUserId());
    }

    private UUID getUserIdFromRequest(HttpServletRequest request) {
        return getUserIdFromToken(extractToken(request));
    }
}
