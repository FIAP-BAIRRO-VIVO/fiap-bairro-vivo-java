package br.com.bairrovivo.dto.ia;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrientacaoRequest(@NotNull @Positive Long ocorrenciaId) {
}
