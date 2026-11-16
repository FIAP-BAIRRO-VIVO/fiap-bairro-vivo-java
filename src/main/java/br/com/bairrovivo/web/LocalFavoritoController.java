package br.com.bairrovivo.web;

import br.com.bairrovivo.dto.PaginaResposta;
import br.com.bairrovivo.dto.usuario.LocalFavoritoRequest;
import br.com.bairrovivo.dto.usuario.LocalFavoritoResponse;
import br.com.bairrovivo.service.LocalFavoritoService;
import br.com.bairrovivo.service.UsuarioId;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usuarios/me/locais-favoritos")
@PreAuthorize("hasRole('CIDADAO')")
public class LocalFavoritoController {
    private final LocalFavoritoService locais;

    public LocalFavoritoController(LocalFavoritoService locais) {
        this.locais = locais;
    }

    @GetMapping
    public PaginaResposta<LocalFavoritoResponse> listar(Authentication authentication,
                                                         @PageableDefault(size = 10) Pageable pageable) {
        return locais.listar(UsuarioId.de(authentication), pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LocalFavoritoResponse criar(Authentication authentication,
                                        @Valid @RequestBody LocalFavoritoRequest request) {
        return locais.criar(UsuarioId.de(authentication), request);
    }

    @GetMapping("/{id}")
    public LocalFavoritoResponse obter(Authentication authentication, @PathVariable Long id) {
        return locais.obter(UsuarioId.de(authentication), id);
    }

    @PutMapping("/{id}")
    public LocalFavoritoResponse atualizar(Authentication authentication, @PathVariable Long id,
                                           @Valid @RequestBody LocalFavoritoRequest request) {
        return locais.atualizar(UsuarioId.de(authentication), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(Authentication authentication, @PathVariable Long id) {
        locais.excluir(UsuarioId.de(authentication), id);
    }
}
