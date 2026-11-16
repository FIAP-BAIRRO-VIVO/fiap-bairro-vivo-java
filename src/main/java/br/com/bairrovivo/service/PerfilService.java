package br.com.bairrovivo.service;

import br.com.bairrovivo.domain.Usuario;
import br.com.bairrovivo.dto.usuario.AtualizarPerfilRequest;
import br.com.bairrovivo.dto.usuario.PerfilResponse;
import br.com.bairrovivo.exception.ApiException;
import br.com.bairrovivo.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PerfilService {
    private final UsuarioRepository usuarios;

    public PerfilService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public PerfilResponse obter(Long usuarioId) {
        return resposta(buscarAtivo(usuarioId));
    }

    @Transactional
    public PerfilResponse atualizar(Long usuarioId, AtualizarPerfilRequest request) {
        Usuario usuario = buscarAtivo(usuarioId);
        usuario.setNome(request.nome().trim());
        return resposta(usuario);
    }

    @Transactional
    public void desativar(Long usuarioId) {
        Usuario usuario = buscarAtivo(usuarioId);
        usuario.desativar();
    }

    private Usuario buscarAtivo(Long id) {
        return usuarios.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new ApiException("USUARIO_NAO_ENCONTRADO",
                        "Conta nao encontrada ou desativada.", HttpStatus.NOT_FOUND));
    }

    private PerfilResponse resposta(Usuario usuario) {
        return new PerfilResponse(usuario.getId().toString(), usuario.getNome(), usuario.getEmail(),
                usuario.getPapel(), usuario.getCriadoEm(), usuario.getAtualizadoEm());
    }
}
