package id.ac.ui.cs.advprog.bechat.exception;

import id.ac.ui.cs.advprog.bechat.dto.BaseResponseDTO;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponseDTO<Map<String, String>>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(e -> e.getField(), e -> e.getDefaultMessage()));
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(BaseResponseDTO.<Map<String, String>>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message("Validation error")
                    .timestamp(new Date())
                    .data(errors)
                    .build());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<BaseResponseDTO<String>> handleConstraintViolation(ConstraintViolationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BaseResponseDTO.error(HttpStatus.BAD_REQUEST.value(), "Constraint violation: " + ex.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<BaseResponseDTO<String>> handleAuthentication(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(BaseResponseDTO.error(HttpStatus.UNAUTHORIZED.value(), ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BaseResponseDTO<String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BaseResponseDTO.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<BaseResponseDTO<String>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT) // bisa juga 400
                .body(BaseResponseDTO.error(HttpStatus.CONFLICT.value(), ex.getMessage()));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<BaseResponseDTO<String>> handleSecurity(SecurityException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(BaseResponseDTO.error(HttpStatus.FORBIDDEN.value(), ex.getMessage()));
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<BaseResponseDTO<String>> handleNullPointer(NullPointerException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseResponseDTO.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Null value encountered"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponseDTO<String>> handleAllOther(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseResponseDTO.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unexpected error: " + ex.getClass().getSimpleName()));
    }
}
