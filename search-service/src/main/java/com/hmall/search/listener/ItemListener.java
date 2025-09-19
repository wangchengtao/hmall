package com.hmall.search.listener;

import com.hmall.api.client.ItemClient;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

// @Component
@RequiredArgsConstructor
public class ItemListener {

    private final ItemClient itemClient;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "item.create.queue", durable = "true"),
            exchange = @Exchange(name = "item.direct", type = ExchangeTypes.DIRECT),
            key = "item.create"
    ))
    public void CreateItemListener(String msg) {
        // TODO
    }
}
