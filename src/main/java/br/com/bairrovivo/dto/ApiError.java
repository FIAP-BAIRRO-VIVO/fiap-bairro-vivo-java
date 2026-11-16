package br.com.bairrovivo.dto;

import java.util.List;

public record ApiError(String codigo, String mensagem, List<DetalheErro> detalhes, String correlationId) {
    public record DetalheErro(String campo, String mensagem) {
    }
}
