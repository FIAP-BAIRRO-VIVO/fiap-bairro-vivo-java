package br.com.bairrovivo.dto.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AtualizarPerfilRequest(@NotBlank @Size(max = 120) String nome) {
}
