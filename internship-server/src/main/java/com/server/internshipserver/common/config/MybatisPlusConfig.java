package com.server.internshipserver.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis Plus配置类
 * 配置MyBatis Plus的分页插件和自动填充功能
 */
@Configuration
@MapperScan("com.server.internshipserver.mapper")
public class MybatisPlusConfig {

    /**
     * 配置MyBatis Plus拦截器
     * 添加分页插件，支持MySQL数据库的分页查询
     * 
     * @return MybatisPlusInterceptor MyBatis Plus拦截器实例
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    /**
     * MyBatis Plus自动填充处理器
     * 在插入和更新操作时自动填充创建时间和更新时间字段
     */
    @Component
    public static class MyMetaObjectHandler implements MetaObjectHandler {
        
        /**
         * 插入时自动填充
         * 自动设置createTime和updateTime为当前时间
         * 
         * @param metaObject 元数据对象
         */
        @Override
        public void insertFill(MetaObject metaObject) {
            this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
            this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        }

        /**
         * 更新时自动填充
         * 自动设置updateTime为当前时间
         * 
         * @param metaObject 元数据对象
         */
        @Override
        public void updateFill(MetaObject metaObject) {
            this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        }
    }
}

