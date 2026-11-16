package br.com.bairrovivo.web;

import br.com.bairrovivo.dto.usuario.PreferenciaRequest;
import br.com.bairrovivo.dto.usuario.PreferenciaResponse;
import br.com.bairrovivo.service.PreferenciaService;
import br.com.bairrovivo.service.UsuarioId;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usuarios/me/preferencias")
@PreAuthorize("hasAnyRole('CIDADAO', 'ADMIN')")
public class PreferenciaController {
    private final PreferenciaService preferencias;

    public PreferenciaController(PreferenciaService preferencias) {
        this.preferencias = preferencias;
    }

    @GetMapping
    public PreferenciaResponse obter(Authentication authentication) {
        return preferencias.obter(UsuarioId.de(authentication));
    }

    @PutMapping
    public PreferenciaResponse atualizar(Authentication authentication,
                                         @Valid @RequestBody PreferenciaRequest request) {
        return preferencias.atualizar(UsuarioId.de(authentication), request);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.OK)
    public PreferenciaResponse restaurar(Authentication authentication) {
        return preferencias.restaurarPadrao(UsuarioId.de(authentication));
    }
}
