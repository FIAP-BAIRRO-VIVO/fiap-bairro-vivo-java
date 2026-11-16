package br.com.bairrovivo.messaging;

import java.time.Instant;
import java.util.UUID;

public record UsuarioCadastradoMessage(
        UUID eventId,
        String type,
        int versao,
        Instant occurredAt,
        Long usuarioId) {
}
