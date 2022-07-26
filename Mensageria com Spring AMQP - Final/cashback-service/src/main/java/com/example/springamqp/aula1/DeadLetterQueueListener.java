package com.example.springamqp.aula1;

import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class DeadLetterQueueListener {

  private static final String DLQ = "orders.v1.order-created.dlx.generate-cashback.dlq";

  private static final String X_RETRY_HEADER = " x-dql-retry";
  private RabbitTemplate rabbitTemplate;

  public DeadLetterQueueListener(RabbitTemplate rabbitTemplate){
    this.rabbitTemplate = rabbitTemplate;
  }


  @RabbitListener(queues = DLQ)
  public void processar(OrderCreatedEvent orderCreatedEvent, @Headers Map<String,Object> headers){
    Integer retryHeader = (Integer) headers.get(X_RETRY_HEADER);
    if(retryHeader == null){
      retryHeader = 0;
    }
    System.out.println("REPROCESSANDO VENDA DE ID" + orderCreatedEvent.getId());

    if(retryHeader < 3){
      int tryCount = retryHeader + 1;
      Map<String,Object> updatedHeaders = new HashMap<>(headers);
      updatedHeaders.put(X_RETRY_HEADER,tryCount);

      final MessagePostProcessor messagePostProcessor = message -> {
        MessageProperties messagePostProperties = message.getMessageProperties();
        updatedHeaders.forEach(messagePostProperties::setHeader);
        return message;
      };
      System.out.println("Reenviando venda de id" + orderCreatedEvent.getId() + "para DLQ");
      this.rabbitTemplate.convertAndSend(DLQ, orderCreatedEvent, messagePostProcessor);
    }else{
      System.out.println("reprocessamento falohu, enviando venda de id " + orderCreatedEvent.getId() + "para o parking lot");
      this.rabbitTemplate.convertAndSend("orders.v1.order-created.dlx.generate-cashback.dlq-parking-lot",orderCreatedEvent);

    }
  }
}
