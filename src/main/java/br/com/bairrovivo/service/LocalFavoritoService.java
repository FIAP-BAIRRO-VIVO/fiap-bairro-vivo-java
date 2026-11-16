package br.com.bairrovivo.service;

import br.com.bairrovivo.domain.LocalFavorito;
import br.com.bairrovivo.domain.Usuario;
import br.com.bairrovivo.dto.PaginaResposta;
import br.com.bairrovivo.dto.usuario.LocalFavoritoRequest;
import br.com.bairrovivo.dto.usuario.LocalFavoritoResponse;
import br.com.bairrovivo.exception.ApiException;
import br.com.bairrovivo.repository.LocalFavoritoRepository;
import br.com.bairrovivo.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LocalFavoritoService {
    private final LocalFavoritoRepository locais;
    private final UsuarioRepository usuarios;

    public LocalFavoritoService(LocalFavoritoRepository locais, UsuarioRepository usuarios) {
        this.locais = locais;
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public PaginaResposta<LocalFavoritoResponse> listar(Long usuarioId, Pageable pageable) {
        validarPaginacao(pageable);
        return PaginaResposta.de(locais.findAllByUsuarioId(usuarioId, pageable), this::resposta);
    }

    @Transactional(readOnly = true)
    public LocalFavoritoResponse obter(Long usuarioId, Long localId) {
        return resposta(buscarDoUsuario(usuarioId, localId));
    }

    @Transactional
    public LocalFavoritoResponse criar(Long usuarioId, LocalFavoritoRequest request) {
        Usuario usuario = usuarios.findByIdAndAtivoTrue(usuarioId)
                .orElseThrow(() -> new ApiException("USUARIO_NAO_ENCONTRADO",
                        "Conta nao encontrada ou desativada.", HttpStatus.NOT_FOUND));
        LocalFavorito local = new LocalFavorito(usuario, request.apelido().trim(),
                request.latitude(), request.longitude());
        return resposta(locais.save(local));
    }

    @Transactional
    public LocalFavoritoResponse atualizar(Long usuarioId, Long localId, LocalFavoritoRequest request) {
        LocalFavorito local = buscarDoUsuario(usuarioId, localId);
        local.setApelido(request.apelido().trim());
        local.setLatitude(request.latitude());
        local.setLongitude(request.longitude());
        return resposta(local);
    }

    @Transactional
    public void excluir(Long usuarioId, Long localId) {
        locais.delete(buscarDoUsuario(usuarioId, localId));
    }

    private LocalFavorito buscarDoUsuario(Long usuarioId, Long localId) {
        return locais.findByIdAndUsuarioId(localId, usuarioId)
                .orElseThrow(() -> new ApiException("LOCAL_NAO_ENCONTRADO",
                        "Local favorito nao encontrado.", HttpStatus.NOT_FOUND));
    }

    private void validarPaginacao(Pageable pageable) {
        if (pageable.getPageSize() > 100) {
            throw new ApiException("PAGINACAO_INVALIDA",
                    "O tamanho maximo da pagina e 100.", HttpStatus.BAD_REQUEST);
        }
    }

    private LocalFavoritoResponse resposta(LocalFavorito local) {
        return new LocalFavoritoResponse(local.getId().toString(), local.getApelido(),
                local.getLatitude(), local.getLongitude(), local.getCriadoEm());
    }
}
