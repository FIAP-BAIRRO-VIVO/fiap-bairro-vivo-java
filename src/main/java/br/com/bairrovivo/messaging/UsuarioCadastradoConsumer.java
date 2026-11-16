package br.com.bairrovivo.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class UsuarioCadastradoConsumer {
    private static final Logger logger = LoggerFactory.getLogger(UsuarioCadastradoConsumer.class);

    @RabbitListener(queues = "${app.rabbitmq.queue}")
    public void consumir(UsuarioCadastradoMessage event) {
        if (!"usuario.cadastrado".equals(event.type()) || event.usuarioId() == null || event.eventId() == null) {
            throw new IllegalArgumentException("Evento usuario.cadastrado com formato invalido.");
        }
        logger.info("Evento usuario.cadastrado consumido eventId={} usuarioId={} versao={}",
                event.eventId(), event.usuarioId(), event.versao());
    }
}
