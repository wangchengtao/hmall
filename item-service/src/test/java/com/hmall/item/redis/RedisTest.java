package com.hmall.item.redis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest(properties = {
        "spring.config.activate.on-profile=local",
        "seata.enabled=false",
})
public class RedisTest {

    @Autowired
    public StringRedisTemplate stringRedisTemplate;

    @Test
    public void setString() {
        stringRedisTemplate.opsForValue().set("name", "hmall");

        System.out.println(stringRedisTemplate.opsForValue().get("name"));
    }
}
