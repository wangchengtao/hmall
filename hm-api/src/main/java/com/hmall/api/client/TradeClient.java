package com.hmall.api.client;

import com.hmall.api.client.fallback.TradeClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "trade-service", fallbackFactory = TradeClientFallback.class)
public interface TradeClient {

    @PutMapping("Orders/{orderId}")
    Long markOrderPaySuccess(@PathVariable("orderId") Long orderId);
}
