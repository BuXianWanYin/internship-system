package com.server.internshipserver.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis连接检查
 * 在应用启动完成后检查Redis连接，如果连接失败则阻止应用继续运行
 */
@Component
public class RedisConnectionCheck implements ApplicationListener<ApplicationReadyEvent> {
    
    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(RedisConnectionCheck.class);
    
    /**
     * Redis模板
     */
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * Redis连接工厂
     */
    @Autowired
    private RedisConnectionFactory redisConnectionFactory;
    
    /**
     * 应用启动完成事件处理
     * 在应用启动完成后检查Redis连接是否正常
     * 
     * @param event 应用启动完成事件
     * @throws RuntimeException 如果Redis连接失败
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        logger.info("开始检查Redis连接...");
        try {
            // 尝试获取Redis连接
            redisConnectionFactory.getConnection().ping();
            // 尝试执行一个简单的Redis操作来检查连接
            redisTemplate.opsForValue().get("__connection_check__");
            logger.info("✓ Redis连接检查成功！");
        } catch (Exception e) {
            logger.error("✗ Redis连接检查失败！请确保Redis服务已启动。", e);
            logger.error("================================================");
            logger.error("Redis连接失败，应用无法正常运行。请检查：");
            logger.error("1. Redis服务是否已启动");
            logger.error("2. Redis配置是否正确（host: localhost, port: 6379）");
            logger.error("3. 防火墙是否阻止了连接");
            logger.error("================================================");
            // 抛出异常，阻止应用继续运行
            throw new RuntimeException("Redis连接失败，应用无法启动。请先启动Redis服务。", e);
        }
    }
}

