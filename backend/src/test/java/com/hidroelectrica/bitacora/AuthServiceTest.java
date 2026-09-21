package com.hidroelectrica.bitacora;

import com.hidroelectrica.bitacora.dto.request.LoginRequestDTO;
import com.hidroelectrica.bitacora.dto.response.AuthResponseDTO;
import com.hidroelectrica.bitacora.exception.UnauthorizedException;
import com.hidroelectrica.bitacora.model.User;
import com.hidroelectrica.bitacora.repository.UserRepository;
import com.hidroelectrica.bitacora.security.JwtService;
import com.hidroelectrica.bitacora.security.PasswordService;
import com.hidroelectrica.bitacora.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordService passwordService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User("c.mendoza", "hashed_pass", "Carlos Mendoza", "ROLE_OPERATOR", "Turno A (06:00–18:00)");
        sampleUser.setId(2L);
        sampleUser.setActive(true);
    }

    @Test
    @DisplayName("Login exitoso debe retornar token y datos del usuario")
    void testLoginSuccess() {
        LoginRequestDTO request = new LoginRequestDTO("c.mendoza", "op123");

        when(userRepository.findByUsername("c.mendoza")).thenReturn(Optional.of(sampleUser));
        when(passwordService.matches("op123", "hashed_pass")).thenReturn(true);
        when(jwtService.generateToken(eq("c.mendoza"), anyMap())).thenReturn("mock.jwt.token");
        when(jwtService.getExpirationMs()).thenReturn(28800000L);

        AuthResponseDTO response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock.jwt.token", response.token());
        assertEquals("Carlos Mendoza", response.user().nombre());
        assertEquals("operador", response.user().rol());
        assertTrue(response.user().activo());
    }

    @Test
    @DisplayName("Login con contraseña incorrecta debe lanzar UnauthorizedException")
    void testLoginInvalidPassword() {
        LoginRequestDTO request = new LoginRequestDTO("c.mendoza", "wrong_pass");

        when(userRepository.findByUsername("c.mendoza")).thenReturn(Optional.of(sampleUser));
        when(passwordService.matches("wrong_pass", "hashed_pass")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("Login con usuario inactivo debe lanzar UnauthorizedException")
    void testLoginInactiveUser() {
        sampleUser.setActive(false);
        LoginRequestDTO request = new LoginRequestDTO("c.mendoza", "op123");

        when(userRepository.findByUsername("c.mendoza")).thenReturn(Optional.of(sampleUser));
        when(passwordService.matches("op123", "hashed_pass")).thenReturn(true);

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> authService.login(request));
        assertTrue(ex.getMessage().contains("inactivo"));
    }
}
