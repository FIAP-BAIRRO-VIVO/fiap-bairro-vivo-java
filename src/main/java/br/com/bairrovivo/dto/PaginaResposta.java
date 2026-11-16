package br.com.bairrovivo.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

public record PaginaResposta<T>(List<T> conteudo, int pagina, int tamanho, long totalElementos, int totalPaginas) {
    public static <S, T> PaginaResposta<T> de(Page<S> page, Function<S, T> mapper) {
        return new PaginaResposta<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
