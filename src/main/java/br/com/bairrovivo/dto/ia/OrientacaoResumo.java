package br.com.bairrovivo.dto.ia;

import br.com.bairrovivo.domain.EstadoOrientacao;

import java.time.Instant;

public record OrientacaoResumo(
        String id,
        String ocorrenciaId,
        EstadoOrientacao estado,
        String orientacao,
        Instant geradaEm) {
}
