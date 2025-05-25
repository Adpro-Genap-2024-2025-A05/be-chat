package id.ac.ui.cs.advprog.bechat.controller;

import id.ac.ui.cs.advprog.bechat.dto.*;
import id.ac.ui.cs.advprog.bechat.model.ChatMessage;
import id.ac.ui.cs.advprog.bechat.model.ChatSession;
import id.ac.ui.cs.advprog.bechat.service.ChatService;
import id.ac.ui.cs.advprog.bechat.service.TokenVerificationService;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import io.micrometer.core.instrument.Timer;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;
    private final TokenVerificationService tokenVerificationService;
    private final MeterRegistry meterRegistry;

    @PostMapping("/send")
    public CompletableFuture<ResponseEntity<BaseResponseDTO<ChatMessage>>> sendMessage(
            @Valid @RequestBody SendMessageRequest dto,
            HttpServletRequest request
    ) {
        UUID userId = getUserIdFromRequest(request);
        return chatService.sendMessage(dto, userId)
                .thenApply(saved ->
                        ResponseEntity.status(HttpStatus.CREATED)
                                .body(BaseResponseDTO.success(HttpStatus.CREATED.value(), "Message sent successfully", saved)));
    }
    @Timed(value = "chat.message.fetch.timer", percentiles = {0.95})
    @GetMapping("/session/{id}")
    public CompletableFuture<ResponseEntity<BaseResponseDTO<ChatSessionWithMessagesDto>>> getMessages(
            @PathVariable UUID id,
            HttpServletRequest request
    ) {
        UUID userId = getUserIdFromRequest(request);

        Timer.Sample sample = Timer.start(meterRegistry);

        return chatService.getMessages(id, userId).thenCompose(messages -> {
            CompletableFuture<ResponseEntity<BaseResponseDTO<ChatSessionWithMessagesDto>>> responseFuture;

            if (messages.isEmpty()) {
                responseFuture = chatService.findSessionById(id).thenApply(optionalSession -> {
                    ChatSession session = optionalSession.orElseThrow(() ->
                            new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));
                    return buildSessionWithMessagesResponse(session, List.of());
                });
            } else {
                ChatSession session = messages.get(0).getSession();
                responseFuture = CompletableFuture.completedFuture(buildSessionWithMessagesResponse(session, messages));
            }

            return responseFuture.whenComplete((response, throwable) -> {
                sample.stop(Timer.builder("chat_message_fetch_timer_seconds")
                        .description("Time taken to fetch chat messages")
                        .register(meterRegistry));
            });
        });
    }

    @PutMapping("/message/{id}")
    public CompletableFuture<ResponseEntity<BaseResponseDTO<ChatMessage>>> editMessage(
            @PathVariable UUID id,
            @Valid @RequestBody EditMessageRequest requestBody,
            HttpServletRequest request
    ) {
        UUID userId = getUserIdFromRequest(request);
        return chatService.editMessage(id, requestBody.getContent(), userId)
                .thenApply(updated ->
                        ResponseEntity.ok(BaseResponseDTO.success(HttpStatus.OK.value(), "Message updated", updated)));
    }

    @DeleteMapping("/message/{id}")
    public CompletableFuture<ResponseEntity<BaseResponseDTO<ChatMessage>>> deleteMessage(
            @PathVariable UUID id,
            HttpServletRequest request
    ) {
        UUID userId = getUserIdFromRequest(request);
        return chatService.deleteMessage(id, userId)
                .thenApply(deleted ->
                        ResponseEntity.ok(BaseResponseDTO.success(HttpStatus.OK.value(), "Message deleted", deleted)));
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

    private ResponseEntity<BaseResponseDTO<ChatSessionWithMessagesDto>> buildSessionWithMessagesResponse(ChatSession session, List<ChatMessage> messages) {
        var responseDto = ChatSessionWithMessagesDto.builder()
                .sessionId(session.getId())
                .pacilian(session.getPacilian())
                .pacilianName(session.getPacilianName())
                .caregiver(session.getCaregiver())
                .caregiverName(session.getCaregiverName())
                .messages(messages)
                .build();

        return ResponseEntity.ok(BaseResponseDTO.success(HttpStatus.OK.value(), "Messages retrieved", responseDto));
    }
}
