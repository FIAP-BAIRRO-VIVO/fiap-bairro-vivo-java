package br.com.bairrovivo.service.ia;

import br.com.bairrovivo.repository.TrechoRagView;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GuidanceOutputValidatorTest {
    private final GuidanceOutputValidator validator = new GuidanceOutputValidator();

    @Test
    void acceptsContactAndLinkGroundedInApprovedSource() {
        TrechoRagView source = source(
                "https://prefeitura.sp.gov.br/servico",
                "Telefone 156. Atendimento 08:00.");

        assertDoesNotThrow(() -> validator.validar(
                "Telefone 156. Atendimento 08:00. Consulte https://prefeitura.sp.gov.br/servico",
                List.of(source)));
    }

    @Test
    void rejectsContactAndLinkAbsentFromApprovedSources() {
        TrechoRagView source = source(
                "https://prefeitura.sp.gov.br/servico",
                "Consulte o servico municipal.");

        assertThrows(IllegalStateException.class, () -> validator.validar(
                "Ligue 190 ou acesse https://site-inventado.example", List.of(source)));
    }

    private TrechoRagView source(String url, String text) {
        TrechoRagView source = mock(TrechoRagView.class);
        when(source.getTitulo()).thenReturn("Servico oficial");
        when(source.getUrl()).thenReturn(url);
        when(source.getTexto()).thenReturn(text);
        return source;
    }
}
