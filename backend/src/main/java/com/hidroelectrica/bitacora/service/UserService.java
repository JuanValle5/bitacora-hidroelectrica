package com.hidroelectrica.bitacora.service;

import com.hidroelectrica.bitacora.dto.request.UserCreateRequestDTO;
import com.hidroelectrica.bitacora.dto.request.UserUpdateRequestDTO;
import com.hidroelectrica.bitacora.dto.response.UserResponseDTO;
import com.hidroelectrica.bitacora.exception.BusinessRuleException;
import com.hidroelectrica.bitacora.exception.ResourceNotFoundException;
import com.hidroelectrica.bitacora.model.User;
import com.hidroelectrica.bitacora.repository.UserRepository;
import com.hidroelectrica.bitacora.security.PasswordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final AuthService authService;

    public UserService(UserRepository userRepository, PasswordService passwordService, AuthService authService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.authService = authService;
    }

    public List<UserResponseDTO> listUsers() {
        return userRepository.findAllByOrderByFullNameAsc().stream()
            .map(authService::toDTO)
            .toList();
    }

    @Transactional
    public UserResponseDTO createUser(UserCreateRequestDTO dto) {
        if (userRepository.existsByUsername(dto.usuario())) {
            throw new BusinessRuleException("El nombre de usuario ya está registrado");
        }

        String role = "admin".equalsIgnoreCase(dto.rol()) ? "ROLE_ADMIN" : "ROLE_OPERATOR";
        String hash = passwordService.hashPassword(dto.password());

        User user = new User(dto.usuario(), hash, dto.nombre(), role, dto.turno());
        user = userRepository.save(user);

        return authService.toDTO(user);
    }

    @Transactional
    public UserResponseDTO updateUser(Long id, UserUpdateRequestDTO dto) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        user.setFullName(dto.nombre());
        if (dto.rol() != null) {
            user.setRole("admin".equalsIgnoreCase(dto.rol()) ? "ROLE_ADMIN" : "ROLE_OPERATOR");
        }
        if (dto.turno() != null) {
            user.setShift(dto.turno());
        }
        if (dto.password() != null && !dto.password().isBlank()) {
            user.setPasswordHash(passwordService.hashPassword(dto.password()));
        }

        user = userRepository.save(user);
        return authService.toDTO(user);
    }

    @Transactional
    public UserResponseDTO toggleStatus(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        user.setActive(!Boolean.TRUE.equals(user.getActive()));
        user = userRepository.save(user);
        return authService.toDTO(user);
    }
}
