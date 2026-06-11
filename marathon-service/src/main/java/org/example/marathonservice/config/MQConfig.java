package org.example.marathonservice.config;
import org.example.marathonservice.constants.MqConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MQConfig {

    // JSON序列化转换器
    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue registerQueue() {
        return QueueBuilder.durable(MqConstant.REGISTRATION_QUEUE)
                .deadLetterExchange(MqConstant.REGISTRATION_DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(MqConstant.REGISTRATION_DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    public DirectExchange registrationExchange() {
        return new DirectExchange(MqConstant.REGISTRATION_EXCHANGE, true, false);
    }

    @Bean
    public Binding registrationBinding() {
        return BindingBuilder.bind(registerQueue())
                .to(registrationExchange())
                .with(MqConstant.REGISTRATION_ROUTING_KEY);
    }

    @Bean
    public Queue registrationDeadLetterQueue() {
        return QueueBuilder.durable(MqConstant.REGISTRATION_DEAD_LETTER_QUEUE).build();
    }

    @Bean
    public DirectExchange registrationDeadLetterExchange() {
        return new DirectExchange(MqConstant.REGISTRATION_DEAD_LETTER_EXCHANGE, true, false);
    }

    @Bean
    public Binding registrationDeadLetterBinding() {
        return BindingBuilder.bind(registrationDeadLetterQueue())
                .to(registrationDeadLetterExchange())
                .with(MqConstant.REGISTRATION_DEAD_LETTER_ROUTING_KEY);
    }
}
