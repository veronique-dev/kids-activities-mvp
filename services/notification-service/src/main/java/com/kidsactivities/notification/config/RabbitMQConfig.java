package com.kidsactivities.notification.config;

import com.kidsactivities.common.event.RabbitConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;

@Configuration
@EnableRabbit
public class RabbitMQConfig {

    public static final String USER_REGISTERED_QUEUE = "notification.user.registered";
    public static final String BOOKING_CONFIRMED_QUEUE = "notification.booking.confirmed";
    public static final String BOOKING_CANCELLED_QUEUE = "notification.booking.cancelled";

    @Bean
    public TopicExchange kidsEventsExchange() {
        return new TopicExchange(RabbitConstants.EXCHANGE);
    }

    @Bean
    public Queue userRegisteredQueue() {
        return new Queue(USER_REGISTERED_QUEUE);
    }

    @Bean
    public Queue bookingConfirmedQueue() {
        return new Queue(BOOKING_CONFIRMED_QUEUE);
    }

    @Bean
    public Queue bookingCancelledQueue() {
        return new Queue(BOOKING_CANCELLED_QUEUE);
    }

    @Bean
    public Binding userRegisteredBinding(Queue userRegisteredQueue, TopicExchange kidsEventsExchange) {
        return BindingBuilder.bind(userRegisteredQueue)
                .to(kidsEventsExchange)
                .with(RabbitConstants.USER_REGISTERED);
    }

    @Bean
    public Binding bookingConfirmedBinding(Queue bookingConfirmedQueue, TopicExchange kidsEventsExchange) {
        return BindingBuilder.bind(bookingConfirmedQueue)
                .to(kidsEventsExchange)
                .with(RabbitConstants.BOOKING_CONFIRMED);
    }

    @Bean
    public Binding bookingCancelledBinding(Queue bookingCancelledQueue, TopicExchange kidsEventsExchange) {
        return BindingBuilder.bind(bookingCancelledQueue)
                .to(kidsEventsExchange)
                .with(RabbitConstants.BOOKING_CANCELLED);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter messageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        return factory;
    }
}
