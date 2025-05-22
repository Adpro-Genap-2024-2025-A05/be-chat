package id.ac.ui.cs.advprog.bechat.config;

import id.ac.ui.cs.advprog.bechat.dto.TokenVerificationResponseDto;
import id.ac.ui.cs.advprog.bechat.model.enums.Role;
import id.ac.ui.cs.advprog.bechat.service.TokenVerificationService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    private TokenVerificationService tokenVerificationService;
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        tokenVerificationService = mock(TokenVerificationService.class);
        jwtAuthenticationFilter = new JwtAuthenticationFilter(tokenVerificationService);
        SecurityContextHolder.clearContext();
    }

    @Test
    void testDoFilterInternal_withValidToken_shouldSetAuthentication() throws Exception {
        String token = "validToken";
        UUID userId = UUID.randomUUID();

        TokenVerificationResponseDto dto = TokenVerificationResponseDto.builder()
                .valid(true)
                .userId(userId.toString())
                .role(Role.CAREGIVER)
                .email("doctor@example.com")
                .name("Dr. Panda")
                .expiresIn(3600L)
                .build();

        when(tokenVerificationService.verifyToken(token)).thenReturn(dto);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(userId.toString(), auth.getPrincipal());
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("CAREGIVER")));

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_withNoAuthorizationHeader_shouldNotAuthenticate() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(); // no Authorization
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_withInvalidToken_shouldNotThrowException() throws Exception {
        String token = "invalidToken";

        when(tokenVerificationService.verifyToken(token))
                .thenThrow(new RuntimeException("Invalid token"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        assertDoesNotThrow(() -> jwtAuthenticationFilter.doFilterInternal(request, response, filterChain));

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }
}
