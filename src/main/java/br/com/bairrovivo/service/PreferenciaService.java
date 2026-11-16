package br.com.bairrovivo.service;

import br.com.bairrovivo.domain.PreferenciaUsuario;
import br.com.bairrovivo.domain.Tema;
import br.com.bairrovivo.domain.Usuario;
import br.com.bairrovivo.dto.usuario.PreferenciaRequest;
import br.com.bairrovivo.dto.usuario.PreferenciaResponse;
import br.com.bairrovivo.exception.ApiException;
import br.com.bairrovivo.repository.PreferenciaUsuarioRepository;
import br.com.bairrovivo.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PreferenciaService {
    private final PreferenciaUsuarioRepository preferencias;
    private final UsuarioRepository usuarios;

    public PreferenciaService(PreferenciaUsuarioRepository preferencias, UsuarioRepository usuarios) {
        this.preferencias = preferencias;
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public PreferenciaResponse obter(Long usuarioId) {
        return preferencias.findByUsuarioId(usuarioId).map(this::resposta)
                .orElseGet(() -> new PreferenciaResponse(Tema.SISTEMA, false, null));
    }

    @Transactional
    public PreferenciaResponse atualizar(Long usuarioId, PreferenciaRequest request) {
        PreferenciaUsuario preferencia = preferencias.findByUsuarioId(usuarioId)
                .orElseGet(() -> new PreferenciaUsuario(buscarUsuario(usuarioId), request.tema(),
                        request.altoContraste()));
        preferencia.setTema(request.tema());
        preferencia.setAltoContraste(request.altoContraste());
        return resposta(preferencias.save(preferencia));
    }

    @Transactional
    public PreferenciaResponse restaurarPadrao(Long usuarioId) {
        preferencias.deleteByUsuarioId(usuarioId);
        return new PreferenciaResponse(Tema.SISTEMA, false, null);
    }

    private Usuario buscarUsuario(Long id) {
        return usuarios.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new ApiException("USUARIO_NAO_ENCONTRADO",
                        "Conta nao encontrada ou desativada.", HttpStatus.NOT_FOUND));
    }

    private PreferenciaResponse resposta(PreferenciaUsuario preferencia) {
        return new PreferenciaResponse(preferencia.getTema(), preferencia.isAltoContraste(),
                preferencia.getAtualizadoEm());
    }
}
