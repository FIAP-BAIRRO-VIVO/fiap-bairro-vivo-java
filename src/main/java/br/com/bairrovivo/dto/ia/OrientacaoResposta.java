package br.com.bairrovivo.dto.ia;

import br.com.bairrovivo.domain.EstadoOrientacao;

import java.time.Instant;
import java.util.List;

public record OrientacaoResposta(
        String id,
        String ocorrenciaId,
        EstadoOrientacao estado,
        MunicipioResposta municipio,
        String orgaoResponsavel,
        String servico,
        List<CanalResposta> canais,
        String orientacao,
        List<FonteResposta> fontes,
        Instant geradaEm) {
}
