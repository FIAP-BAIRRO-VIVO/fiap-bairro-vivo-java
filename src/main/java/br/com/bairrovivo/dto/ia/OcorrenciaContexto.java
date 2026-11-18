package br.com.bairrovivo.dto.ia;

public record OcorrenciaContexto(
        Long id,
        String titulo,
        String descricao,
        Long categoriaId,
        String categoria,
        Long municipioId,
        String municipio,
        String uf) {
}
