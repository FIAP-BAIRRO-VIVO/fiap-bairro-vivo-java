package br.com.bairrovivo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bairro-vivo.ai.enabled", havingValue = "true")
public class AiCredentialValidator {

    public AiCredentialValidator(@Value("${spring.ai.openai.api-key:disabled}") String apiKey) {
        if (apiKey.isBlank() || "disabled".equals(apiKey)) {
            throw new IllegalStateException(
                    "BAIRROVIVO_AI_ENABLED=true requer uma credencial valida em OPENAI_API_KEY.");
        }
    }
}
