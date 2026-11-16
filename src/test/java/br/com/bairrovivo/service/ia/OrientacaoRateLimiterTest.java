package br.com.bairrovivo.service.ia;

import br.com.bairrovivo.exception.ApiException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrientacaoRateLimiterTest {

    @Test
    void limitaCadaUsuarioACincoSolicitacoesPorJanela() {
        OrientacaoRateLimiter limiter = new OrientacaoRateLimiter();
        for (int request = 0; request < 5; request++) {
            limiter.verificar(7L);
        }

        ApiException exception = assertThrows(ApiException.class, () -> limiter.verificar(7L));

        assertEquals("LIMITE_DE_REQUISICOES", exception.getCodigo());
    }

    @Test
    void mantemLimitesSeparadosPorUsuario() {
        OrientacaoRateLimiter limiter = new OrientacaoRateLimiter();
        for (int request = 0; request < 5; request++) {
            limiter.verificar(7L);
        }

        limiter.verificar(8L);
    }
}
