package com.hmall.api.client.fallback;

import com.hmall.api.client.CartClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.Collection;

@Slf4j
public class CartClientFallback implements FallbackFactory<CartClient> {
    @Override
    public CartClient create(Throwable cause) {
        return new CartClient() {
            @Override
            public void removeByItemIds(Collection<Long> ids) {
                log.error("远程调用 CartClient#removeByItemIds 失败, 参数: {}", ids, cause);
            }
        };
    }
}
