package com.hidroelectrica.bitacora.controller;

import com.hidroelectrica.bitacora.dto.request.LoginRequestDTO;
import com.hidroelectrica.bitacora.dto.response.AuthResponseDTO;
import com.hidroelectrica.bitacora.dto.response.UserResponseDTO;
import com.hidroelectrica.bitacora.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser() {
        return ResponseEntity.ok(authService.getCurrentUser());
    }
}
