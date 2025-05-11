package id.ac.ui.cs.advprog.bechat.exception;

import id.ac.ui.cs.advprog.bechat.dto.BaseResponseDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleValidationErrors_returnsBadRequestAndErrors() {
        // Mock field error
        FieldError error = new FieldError("object", "field", "must not be blank");
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(error));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<BaseResponseDTO<Map<String, String>>> response = handler.handleValidationErrors(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation error", response.getBody().getMessage());
        assertEquals("must not be blank", response.getBody().getData().get("field"));
    }

    @Test
    void testHandleConstraintViolation_returnsBadRequest() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("field is invalid");

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

        ResponseEntity<BaseResponseDTO<String>> response = handler.handleConstraintViolation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("Constraint violation"));
    }

    @Test
    void testHandleRuntime_returnsInternalServerError() {
        RuntimeException ex = new RuntimeException("Something went wrong");

        ResponseEntity<BaseResponseDTO<String>> response = handler.handleAllOther(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("Unexpected error"));
    }

    @Test
    void testHandleNullPointer_returnsInternalServerError() {
        NullPointerException ex = new NullPointerException("null value");

        ResponseEntity<BaseResponseDTO<String>> response = handler.handleNullPointer(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Null value encountered", response.getBody().getMessage());
    }

    @Test
    void testHandleIllegalArgument_returnsBadRequest() {
        IllegalArgumentException ex = new IllegalArgumentException("Bad input");

        ResponseEntity<BaseResponseDTO<String>> response = handler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Bad input", response.getBody().getMessage());
    }

    @Test
    void testHandleIllegalState_returnsConflict() {
        IllegalStateException ex = new IllegalStateException("Invalid state");

        ResponseEntity<BaseResponseDTO<String>> response = handler.handleIllegalState(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Invalid state", response.getBody().getMessage());
    }

    @Test
    void testHandleSecurity_returnsForbidden() {
        SecurityException ex = new SecurityException("Forbidden access");

        ResponseEntity<BaseResponseDTO<String>> response = handler.handleSecurity(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Forbidden access", response.getBody().getMessage());
    }

    @Test
    void testHandleAuthentication_returnsUnauthorized() {
        AuthenticationException ex = new AuthenticationException("Invalid token");

        ResponseEntity<BaseResponseDTO<String>> response = handler.handleAuthentication(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid token", response.getBody().getMessage());
    }
}
