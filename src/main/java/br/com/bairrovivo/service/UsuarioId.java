package br.com.bairrovivo.service;

import br.com.bairrovivo.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

public final class UsuarioId {
    private UsuarioId() {
    }

    public static Long de(Authentication authentication) {
        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException exception) {
            throw new ApiException("IDENTIDADE_INVALIDA",
                    "O identificador do usuario autenticado e invalido.", HttpStatus.UNAUTHORIZED);
        }
    }
}
