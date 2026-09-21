package com.hidroelectrica.bitacora.controller;

import com.hidroelectrica.bitacora.dto.request.UserCreateRequestDTO;
import com.hidroelectrica.bitacora.dto.request.UserUpdateRequestDTO;
import com.hidroelectrica.bitacora.dto.response.UserResponseDTO;
import com.hidroelectrica.bitacora.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> listUsers() {
        return ResponseEntity.ok(userService.listUsers());
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserCreateRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequestDTO dto) {
        return ResponseEntity.ok(userService.updateUser(id, dto));
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<UserResponseDTO> toggleStatus(@PathVariable Long id) {
        return ResponseEntity.ok(userService.toggleStatus(id));
    }
}
