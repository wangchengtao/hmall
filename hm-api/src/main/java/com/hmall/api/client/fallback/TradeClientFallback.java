package com.hmall.api.client.fallback;

import com.hmall.api.client.TradeClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

@Slf4j
public class TradeClientFallback implements FallbackFactory<TradeClient> {
    @Override
    public TradeClient create(Throwable cause) {
        return new TradeClient() {
            @Override
            public Long markOrderPaySuccess(Long orderId) {
                log.error("远程调用 TradeClient#markOrderPaySuccess 方法出现异常, 参数{}", orderId, cause);
                return 1L;
            }
        };
    }
}
