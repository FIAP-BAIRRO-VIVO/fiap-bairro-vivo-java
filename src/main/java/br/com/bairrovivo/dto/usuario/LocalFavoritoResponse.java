package br.com.bairrovivo.dto.usuario;

import java.math.BigDecimal;
import java.time.Instant;

public record LocalFavoritoResponse(
        String id,
        String apelido,
        BigDecimal latitude,
        BigDecimal longitude,
        Instant criadoEm) {
}
