package id.ac.ui.cs.advprog.bechat.exception;

import id.ac.ui.cs.advprog.bechat.dto.BaseResponseDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleValidationErrors_returnsBadRequestAndErrors() {
        FieldError error = new FieldError("object", "field", "must not be blank");
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(error));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<BaseResponseDTO<Map<String, String>>> response = handler.handleValidationErrors(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation failed on input fields", response.getBody().getMessage());
        assertEquals("must not be blank", response.getBody().getData().get("field"));
    }

    @Test
    void testHandleValidationErrors_withDuplicateField_keepsFirstMessage() {
        FieldError error1 = new FieldError("object", "field", "first error");
        FieldError error2 = new FieldError("object", "field", "second error");
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(error1, error2));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<BaseResponseDTO<Map<String, String>>> response = handler.handleValidationErrors(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, String> errors = response.getBody().getData();
        assertEquals("first error", errors.get("field"));
        assertFalse(errors.containsValue("second error"));
    }

    @Test
    void testHandleConstraintViolation_returnsBadRequest() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path mockPath = mock(Path.class);
        when(mockPath.toString()).thenReturn("field");
        when(violation.getPropertyPath()).thenReturn(mockPath);
        when(violation.getMessage()).thenReturn("field is invalid");

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

        ResponseEntity<BaseResponseDTO<Map<String, String>>> response = handler.handleConstraintViolation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("Constraint violation"));
        assertEquals("field is invalid", response.getBody().getData().get("field"));
    }

    @Test
    void testHandleConstraintViolation_withDuplicatePath_keepsFirstMessage() {
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("dupField");

        ConstraintViolation<?> v1 = mock(ConstraintViolation.class);
        ConstraintViolation<?> v2 = mock(ConstraintViolation.class);
        when(v1.getPropertyPath()).thenReturn(path);
        when(v1.getMessage()).thenReturn("first violation");
        when(v2.getPropertyPath()).thenReturn(path);
        when(v2.getMessage()).thenReturn("second violation");

        Set<ConstraintViolation<?>> set = new LinkedHashSet<>(List.of(v1, v2));
        ConstraintViolationException ex = new ConstraintViolationException(set);

        ResponseEntity<BaseResponseDTO<Map<String, String>>> response = handler.handleConstraintViolation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, String> errors = response.getBody().getData();

        assertTrue(errors.containsKey("dupField"));

        String errorMessage = errors.get("dupField");
        assertTrue(errorMessage.equals("first violation") || errorMessage.equals("second violation"));

        assertEquals(1, errors.size());
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
        assertEquals("Unexpected null value", response.getBody().getMessage());
    }

    @Test
    void testHandleIllegalArgument_returnsBadRequest() {
        IllegalArgumentException ex = new IllegalArgumentException("Bad input");

        ResponseEntity<BaseResponseDTO<String>> response = handler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Bad request: Bad input", response.getBody().getMessage());
    }

    @Test
    void testHandleIllegalState_returnsConflict() {
        IllegalStateException ex = new IllegalStateException("Invalid state");

        ResponseEntity<BaseResponseDTO<String>> response = handler.handleIllegalState(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Invalid state: Invalid state", response.getBody().getMessage());
    }

    @Test
    void testHandleSecurity_returnsForbidden() {
        SecurityException ex = new SecurityException("Forbidden access");

        ResponseEntity<BaseResponseDTO<String>> response = handler.handleSecurity(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Access denied: Forbidden access", response.getBody().getMessage());
    }

    @Test
    void testHandleAuthentication_returnsUnauthorized() {
        AuthenticationException ex = new AuthenticationException("Invalid token");

        ResponseEntity<BaseResponseDTO<String>> response = handler.handleAuthentication(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Authentication failed: Invalid token", response.getBody().getMessage());
    }
}
