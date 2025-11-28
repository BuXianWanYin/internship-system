package com.server.internshipserver.common.utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.server.internshipserver.common.enums.DeleteFlag;

import java.util.Collection;

/**
 * 查询Wrapper工具类
 * 用于封装MyBatis Plus查询条件构建的重复逻辑，提供常用的查询条件构建方法
 */
public final class QueryWrapperUtil {
    
    private QueryWrapperUtil() {
        // 工具类，禁止实例化
    }
    
    /**
     * 在查询Wrapper中添加未删除条件
     * 
     * @param wrapper 查询Wrapper
     * @param deleteFlagGetter 删除标志字段的getter方法引用
     * @param <T> 实体类型
     * @return 添加了未删除条件的Wrapper
     */
    public static <T> LambdaQueryWrapper<T> notDeleted(LambdaQueryWrapper<T> wrapper, SFunction<T, Integer> deleteFlagGetter) {
        return wrapper.eq(deleteFlagGetter, DeleteFlag.NORMAL.getCode());
    }
    
    /**
     * 创建新的查询Wrapper并添加未删除条件
     * 
     * @param deleteFlagGetter 删除标志字段的getter方法引用
     * @param <T> 实体类型
     * @return 包含未删除条件的Wrapper
     */
    public static <T> LambdaQueryWrapper<T> buildNotDeletedWrapper(SFunction<T, Integer> deleteFlagGetter) {
        return new LambdaQueryWrapper<T>().eq(deleteFlagGetter, DeleteFlag.NORMAL.getCode());
    }
    
    /**
     * 如果集合非空，则添加IN条件
     * 
     * @param wrapper 查询Wrapper
     * @param column 字段getter方法引用
     * @param values 值集合
     * @param <T> 实体类型
     * @param <R> 字段类型
     * @return 添加了IN条件的Wrapper（如果集合非空）
     */
    public static <T, R> LambdaQueryWrapper<T> inIfNotEmpty(LambdaQueryWrapper<T> wrapper, 
                                                             SFunction<T, R> column, 
                                                             Collection<R> values) {
        if (EntityValidationUtil.isNotEmpty(values)) {
            wrapper.in(column, values);
        }
        return wrapper;
    }
}

