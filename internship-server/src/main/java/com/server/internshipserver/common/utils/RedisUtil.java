package com.server.internshipserver.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis工具类
 * 封装Redis的常用操作，包括设置、获取、删除、判断存在、设置过期时间等
 */
@Component
public class RedisUtil {
    
    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(RedisUtil.class);
    
    /**
     * Redis模板
     */
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 设置键值对
     */
    public void set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            logger.error("Redis设置值失败: key={}, error={}", key, e.getMessage());
        }
    }

    /**
     * 设置键值对（带过期时间）
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        } catch (Exception e) {
            logger.error("Redis设置值失败: key={}, timeout={}, error={}", key, timeout, e.getMessage());
        }
    }

    /**
     * 获取值
     */
    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            logger.error("Redis获取值失败: key={}, error={}", key, e.getMessage());
            return null;
        }
    }

    /**
     * 获取值（指定类型）
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            return value != null ? (T) value : null;
        } catch (Exception e) {
            logger.error("Redis获取值失败: key={}, error={}", key, e.getMessage());
            return null;
        }
    }

    /**
     * 删除键
     */
    public Boolean delete(String key) {
        try {
            return redisTemplate.delete(key);
        } catch (Exception e) {
            logger.error("Redis删除键失败: key={}, error={}", key, e.getMessage());
            return false;
        }
    }

    /**
     * 判断键是否存在
     */
    public Boolean exists(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            logger.error("Redis判断键是否存在失败: key={}, error={}", key, e.getMessage());
            return false;
        }
    }

    /**
     * 设置过期时间
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            return redisTemplate.expire(key, timeout, unit);
        } catch (Exception e) {
            logger.error("Redis设置过期时间失败: key={}, timeout={}, error={}", key, timeout, e.getMessage());
            return false;
        }
    }

    /**
     * 获取过期时间
     */
    public Long getExpire(String key) {
        try {
            return redisTemplate.getExpire(key);
        } catch (Exception e) {
            logger.error("Redis获取过期时间失败: key={}, error={}", key, e.getMessage());
            return -1L;
        }
    }
}

