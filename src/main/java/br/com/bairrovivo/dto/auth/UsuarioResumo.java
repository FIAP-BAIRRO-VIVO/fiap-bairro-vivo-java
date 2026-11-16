package br.com.bairrovivo.dto.auth;

import br.com.bairrovivo.domain.Papel;

public record UsuarioResumo(String id, String nome, String email, Papel papel) {
}
