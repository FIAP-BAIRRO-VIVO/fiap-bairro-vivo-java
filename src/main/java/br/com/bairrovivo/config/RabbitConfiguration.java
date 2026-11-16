package br.com.bairrovivo.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfiguration {

    @Bean
    DirectExchange bairroVivoExchange() {
        return new DirectExchange("bairro-vivo.events", true, false);
    }

    @Bean
    Queue usuarioCadastradoQueue() {
        return QueueBuilder.durable("bairro-vivo.user-registered").build();
    }

    @Bean
    Binding usuarioCadastradoBinding(Queue usuarioCadastradoQueue, DirectExchange bairroVivoExchange) {
        return BindingBuilder.bind(usuarioCadastradoQueue)
                .to(bairroVivoExchange)
                .with("usuario.cadastrado");
    }

    @Bean
    Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
