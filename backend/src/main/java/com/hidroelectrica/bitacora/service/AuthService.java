package com.hidroelectrica.bitacora.service;

import com.hidroelectrica.bitacora.dto.request.LoginRequestDTO;
import com.hidroelectrica.bitacora.dto.response.AuthResponseDTO;
import com.hidroelectrica.bitacora.dto.response.UserResponseDTO;
import com.hidroelectrica.bitacora.exception.UnauthorizedException;
import com.hidroelectrica.bitacora.model.User;
import com.hidroelectrica.bitacora.repository.UserRepository;
import com.hidroelectrica.bitacora.security.JwtService;
import com.hidroelectrica.bitacora.security.PasswordService;
import com.hidroelectrica.bitacora.security.UserContext;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordService passwordService, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
    }

    public AuthResponseDTO login(LoginRequestDTO request) {
        User user = userRepository.findByUsername(request.username())
            .orElseThrow(() -> new UnauthorizedException("Credenciales inválidas. Verifique su usuario y contraseña."));

        if (!passwordService.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Credenciales inválidas. Verifique su usuario y contraseña.");
        }

        if (Boolean.FALSE.equals(user.getActive())) {
            throw new UnauthorizedException("El usuario se encuentra inactivo en el sistema. Contacte al administrador.");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole());
        claims.put("fullName", user.getFullName());
        claims.put("shift", user.getShift());

        String token = jwtService.generateToken(user.getUsername(), claims);
        UserResponseDTO userDTO = toDTO(user);

        return AuthResponseDTO.of(token, jwtService.getExpirationMs() / 1000, userDTO);
    }

    public UserResponseDTO getCurrentUser() {
        User current = UserContext.getCurrentUser();
        if (current == null) {
            throw new UnauthorizedException("No hay sesión de usuario activa");
        }
        return toDTO(current);
    }

    public UserResponseDTO toDTO(User user) {
        String roleStr = user.getRole().replace("ROLE_", "").toLowerCase();
        if ("operator".equalsIgnoreCase(roleStr)) {
            roleStr = "operador";
        }
        return new UserResponseDTO(
            user.getId(),
            user.getFullName(),
            user.getUsername(),
            roleStr,
            Boolean.TRUE.equals(user.getActive()),
            user.getShift() != null ? user.getShift() : "—"
        );
    }
}
