package com.cffex.simulatedtradingmodel.config;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Component
public class RedisInitializer {

    @Resource
    private RedisTemplate redisTemplate;

    @PostConstruct
    public void init() {
        // 设置初始值
        redisTemplate.opsForValue().setIfAbsent("order_id_select", "0");
    }
}