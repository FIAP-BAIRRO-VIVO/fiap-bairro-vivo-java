package br.com.bairrovivo.dto.usuario;

import br.com.bairrovivo.domain.Papel;

import java.time.Instant;

public record PerfilResponse(String id, String nome, String email, Papel papel, Instant criadoEm, Instant atualizadoEm) {
}
