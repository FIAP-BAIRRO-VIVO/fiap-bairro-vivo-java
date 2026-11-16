package br.com.bairrovivo.dto.usuario;

import br.com.bairrovivo.domain.Tema;

import java.time.Instant;

public record PreferenciaResponse(Tema tema, boolean altoContraste, Instant atualizadoEm) {
}
