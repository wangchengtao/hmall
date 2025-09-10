package com.hmall.common.utils;

import cn.hutool.core.lang.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.util.concurrent.ListenableFutureCallback;


@Slf4j
@RequiredArgsConstructor
public class RabbitMqHelper {

    private final RabbitTemplate rabbitTemplate;

    public void sendMessage(String exchange, String routingKey, Object message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }

    public void sendDelayMessage(String exchange, String routingKey, Object message, int delayTIme) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message, message1 -> {
            message1.getMessageProperties().setDelay(delayTIme);
            return message1;
        });
    }

    public void sendMessageWithConfirm(String exchange, String routingKey, Object message, int maxRetries) {
        CorrelationData cd = new CorrelationData(UUID.randomUUID().toString(true));
        cd.getFuture().addCallback(new ListenableFutureCallback<CorrelationData.Confirm>() {
            @Override
            public void onFailure(Throwable ex) {
                log.error("处理 ack 回执失败", ex);
            }

            int retryCount = 0;

            @Override
            public void onSuccess(CorrelationData.Confirm result) {

                if (result != null && !result.isAck()) {
                    log.debug("消息发送失败, 收到 NACK, 已重试次数:{}", retryCount++);

                    if (retryCount >= maxRetries) {
                        log.error("消息发送重试次数耗尽, 发送失败");
                        return;
                    }

                    CorrelationData cd1 = new CorrelationData(UUID.randomUUID().toString());
                    cd1.getFuture().addCallback(this);
                    rabbitTemplate.convertAndSend(exchange, routingKey, message, cd1);
                }
            }
        });
        rabbitTemplate.convertAndSend(exchange, routingKey, message, cd);
    }
}
