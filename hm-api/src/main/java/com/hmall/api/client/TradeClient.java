package com.hmall.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "trade-service")
public interface TradeClient {

    @PutMapping("Orders/{orderId}")
    Long markOrderPaySuccess(@PathVariable("orderId") Long orderId);
}
