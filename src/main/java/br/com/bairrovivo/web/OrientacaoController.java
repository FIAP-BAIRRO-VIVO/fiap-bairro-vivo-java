package br.com.bairrovivo.web;

import br.com.bairrovivo.dto.PaginaResposta;
import br.com.bairrovivo.dto.ia.OrientacaoRequest;
import br.com.bairrovivo.dto.ia.OrientacaoResposta;
import br.com.bairrovivo.dto.ia.OrientacaoResumo;
import br.com.bairrovivo.service.UsuarioId;
import br.com.bairrovivo.service.ia.OrientacaoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ia/orientacoes")
public class OrientacaoController {
    private final OrientacaoService orientacoes;

    public OrientacaoController(OrientacaoService orientacoes) {
        this.orientacoes = orientacoes;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CIDADAO')")
    public OrientacaoResposta solicitar(Authentication authentication,
                                        @Valid @RequestBody OrientacaoRequest request) {
        return orientacoes.solicitar(UsuarioId.de(authentication), request.ocorrenciaId());
    }

    @GetMapping("/ocorrencias/{ocorrenciaId}")
    public PaginaResposta<OrientacaoResumo> listarPublicas(
            @PathVariable Long ocorrenciaId,
            @PageableDefault(size = 10) Pageable pageable) {
        return orientacoes.listarPublicas(ocorrenciaId, pageable);
    }

    @GetMapping("/{id}")
    public OrientacaoResumo obterPublica(@PathVariable Long id) {
        return orientacoes.obterPublica(id);
    }
}
