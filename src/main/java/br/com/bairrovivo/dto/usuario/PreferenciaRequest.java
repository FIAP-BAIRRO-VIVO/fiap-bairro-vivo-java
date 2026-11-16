package br.com.bairrovivo.dto.usuario;

import br.com.bairrovivo.domain.Tema;
import jakarta.validation.constraints.NotNull;

public record PreferenciaRequest(@NotNull Tema tema, boolean altoContraste) {
}
