package br.com.bairrovivo.messaging;

import br.com.bairrovivo.service.event.UsuarioCadastradoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.util.UUID;

@Component
public class UsuarioCadastradoPublisher {
    private static final Logger logger = LoggerFactory.getLogger(UsuarioCadastradoPublisher.class);
    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public UsuarioCadastradoPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${app.rabbitmq.exchange}") String exchange,
            @Value("${app.rabbitmq.routing-key}") String routingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publicar(UsuarioCadastradoEvent event) {
        UsuarioCadastradoMessage message = new UsuarioCadastradoMessage(
                UUID.randomUUID(), "usuario.cadastrado", 1, Instant.now(), event.usuarioId());
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, message);
            logger.info("Evento usuario.cadastrado publicado eventId={} usuarioId={}",
                    message.eventId(), message.usuarioId());
        } catch (AmqpException exception) {
            logger.error("Falha ao publicar usuario.cadastrado eventId={} usuarioId={}",
                    message.eventId(), message.usuarioId(), exception);
            throw exception;
        }
    }
}
