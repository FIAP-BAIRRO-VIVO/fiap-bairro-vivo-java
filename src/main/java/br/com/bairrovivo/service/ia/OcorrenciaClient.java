package br.com.bairrovivo.service.ia;

import br.com.bairrovivo.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;

import java.time.Duration;
import java.util.Map;

@Component
public class OcorrenciaClient {
    private final WebClient webClient;
    private final String occurrencePath;
    private final String serviceToken;

    public OcorrenciaClient(
            WebClient.Builder webClientBuilder,
            @Value("${bairro-vivo.dotnet.base-url}") String baseUrl,
            @Value("${bairro-vivo.dotnet.occurrence-path}") String occurrencePath,
            @Value("${bairro-vivo.dotnet.service-token:}") String serviceToken) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.occurrencePath = occurrencePath;
        this.serviceToken = serviceToken;
    }

    public OcorrenciaContexto buscar(Long ocorrenciaId) {
        try {
            OcorrenciaContexto contexto = webClient.get()
                    .uri(uri -> construirUri(uri, ocorrenciaId))
                    .headers(headers -> {
                        if (!serviceToken.isBlank()) {
                            headers.setBearerAuth(serviceToken);
                        }
                    })
                    .retrieve()
                    .bodyToMono(OcorrenciaContexto.class)
                    .block(Duration.ofSeconds(5));
            if (contexto == null) {
                throw new IllegalStateException("A API de comunidade retornou um corpo vazio.");
            }
            return contexto;
        } catch (WebClientResponseException.NotFound exception) {
            throw new ApiException("OCORRENCIA_NAO_ENCONTRADA",
                    "A ocorrencia nao foi encontrada na API de comunidade.", HttpStatus.NOT_FOUND);
        } catch (WebClientResponseException exception) {
            throw new ApiException("API_COMUNIDADE_INDISPONIVEL",
                    "Nao foi possivel consultar a API de ocorrencias.", HttpStatus.BAD_GATEWAY);
        } catch (RuntimeException exception) {
            throw new ApiException("API_COMUNIDADE_INDISPONIVEL",
                    "Nao foi possivel consultar a API de ocorrencias.", HttpStatus.BAD_GATEWAY);
        }
    }

    private java.net.URI construirUri(UriBuilder uriBuilder, Long id) {
        return uriBuilder.path(occurrencePath).build(Map.of("id", id));
    }
}
