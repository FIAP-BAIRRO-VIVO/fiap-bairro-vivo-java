package br.com.bairrovivo.web;

import br.com.bairrovivo.dto.usuario.AtualizarPerfilRequest;
import br.com.bairrovivo.dto.usuario.PerfilResponse;
import br.com.bairrovivo.service.PerfilService;
import br.com.bairrovivo.service.UsuarioId;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usuarios/me")
@PreAuthorize("hasAnyRole('CIDADAO', 'ADMIN')")
public class PerfilController {
    private final PerfilService perfilService;

    public PerfilController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    @GetMapping
    public PerfilResponse obter(Authentication authentication) {
        return perfilService.obter(UsuarioId.de(authentication));
    }

    @PatchMapping
    public PerfilResponse atualizar(Authentication authentication,
                                    @Valid @RequestBody AtualizarPerfilRequest request) {
        return perfilService.atualizar(UsuarioId.de(authentication), request);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desativar(Authentication authentication) {
        perfilService.desativar(UsuarioId.de(authentication));
    }
}
