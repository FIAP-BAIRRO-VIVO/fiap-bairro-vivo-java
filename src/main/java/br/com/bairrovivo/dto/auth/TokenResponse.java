package br.com.bairrovivo.dto.auth;

public record TokenResponse(String accessToken, String tokenType, long expiresIn, UsuarioResumo usuario) {
}
