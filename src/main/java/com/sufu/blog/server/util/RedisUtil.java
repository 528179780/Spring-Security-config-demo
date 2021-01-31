package com.sufu.blog.server.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Redis 工具类
 * @author sufu
 * @date 2021/1/28
 */
public class RedisUtil {
    public static RedisTemplate<String,Object> redisTemplate;
    @Autowired
    public static void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        RedisUtil.redisTemplate = redisTemplate;
    }
}
