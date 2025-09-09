package com.hmall.trade.listener;

import com.hmall.api.client.PayClient;
import com.hmall.api.dto.PayOrderDTO;
import com.hmall.trade.constants.MQConstants;
import com.hmall.trade.domain.po.Order;
import com.hmall.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderDelayMessageListener {

    private final IOrderService orderService;
    private final PayClient payClient;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MQConstants.DELAY_ORDER_QUEUE_NAME),
            exchange = @Exchange(name = MQConstants.DELAY_EXCHANGE_NAME),
            key = MQConstants.DELAY_ORDER_KEY
    ))
    public void listenOrderDelayMessage(Long orderId) {
        Order order = orderService.getById(orderId);

        // 检测订单状态, 判断是否已支付
        if (order == null || order.getStatus() != 1) {
            return;
        }

        PayOrderDTO payOrder = payClient.queryPayOrderByBizOrderNo(order.getId());

        if (payOrder != null && payOrder.getStatus() == 3) {
            // 订单标记为已支付
            orderService.markOrderPaySuccess(orderId);
        } else {
            // 未支付 取消订单,恢复库存
            orderService.cancelOrder(orderId);
        }
    }
}
