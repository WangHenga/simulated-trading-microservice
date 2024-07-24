package com.cffex.simulatedtradingorderservice.mq;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class MessageProducer {
    @Resource
    private RabbitTemplate rabbitTemplate;
    private final String exchange = "trade.cffex";
    private final String routingKey = "trade";
    public void sendMessage(String message){
        rabbitTemplate.convertAndSend(exchange,routingKey,message);
    }
}

