package com.example.springamqp.aula1;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class OrderCreatedListener {

    @RabbitListener(queues = "orders.v1.order-created.generate-cashback")
    public void onOrderCreated(OrderCreatedEvent orderEvent) {
        System.out.println("Id recebido " + orderEvent.getId());
        System.out.println("Valor recebido " + orderEvent.getValue());

        if(orderEvent.getValue().compareTo(new BigDecimal(10000)) >= 0){
            throw new RuntimeException("Falha no processamento da venda de id" + orderEvent.getId());
        }
    }

}
