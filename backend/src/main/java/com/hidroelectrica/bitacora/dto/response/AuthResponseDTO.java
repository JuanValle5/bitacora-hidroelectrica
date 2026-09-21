package com.hidroelectrica.bitacora.dto.response;

public record AuthResponseDTO(
    String token,
    String tokenType,
    long expiresIn,
    UserResponseDTO user
) {
    public static AuthResponseDTO of(String token, long expiresIn, UserResponseDTO user) {
        return new AuthResponseDTO(token, "Bearer", expiresIn, user);
    }
}
