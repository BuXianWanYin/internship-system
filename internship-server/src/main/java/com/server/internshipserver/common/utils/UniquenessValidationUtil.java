package com.server.internshipserver.common.utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.service.IService;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.exception.BusinessException;

/**
 * 唯一性验证工具类
 * 提供字段唯一性验证的方法，支持单字段唯一性和组合字段唯一性验证
 */
public final class UniquenessValidationUtil {
    
    private UniquenessValidationUtil() {
        // 工具类，禁止实例化
    }
    
    /**
     * 验证字段值是否唯一（排除已删除的记录）
     * 
     * @param service Service实例
     * @param fieldGetter 字段getter方法引用
     * @param fieldValue 字段值
     * @param deleteFlagGetter 删除标志字段getter方法引用
     * @param fieldName 字段名称（用于错误消息）
     * @param <T> 实体类型
     * @param <R> 字段类型
     * @throws BusinessException 如果字段值已存在
     */
    public static <T, R> void validateUnique(IService<T> service,
                                             SFunction<T, R> fieldGetter,
                                             R fieldValue,
                                             SFunction<T, Integer> deleteFlagGetter,
                                             String fieldName) {
        if (fieldValue == null) {
            return; // null值不进行唯一性检查
        }
        
        LambdaQueryWrapper<T> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(fieldGetter, fieldValue)
               .eq(deleteFlagGetter, DeleteFlag.NORMAL.getCode());
        
        T existEntity = service.getOne(wrapper);
        if (existEntity != null) {
            throw new BusinessException(fieldName + "已存在");
        }
    }
    
    /**
     * 验证字段值是否唯一（排除指定ID和已删除的记录）
     * 用于更新操作时检查唯一性
     * 
     * @param service Service实例
     * @param fieldGetter 字段getter方法引用
     * @param fieldValue 字段值
     * @param idGetter ID字段getter方法引用
     * @param excludeId 要排除的ID（通常是当前记录的ID）
     * @param deleteFlagGetter 删除标志字段getter方法引用
     * @param fieldName 字段名称（用于错误消息）
     * @param <T> 实体类型
     * @param <R> 字段类型
     * @param <ID> ID类型
     * @throws BusinessException 如果字段值已存在（排除指定ID）
     */
    public static <T, R, ID> void validateUniqueExcludeId(IService<T> service,
                                                           SFunction<T, R> fieldGetter,
                                                           R fieldValue,
                                                           SFunction<T, ID> idGetter,
                                                           ID excludeId,
                                                           SFunction<T, Integer> deleteFlagGetter,
                                                           String fieldName) {
        if (fieldValue == null || excludeId == null) {
            return;
        }
        
        LambdaQueryWrapper<T> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(fieldGetter, fieldValue)
               .ne(idGetter, excludeId)
               .eq(deleteFlagGetter, DeleteFlag.NORMAL.getCode());
        
        T existEntity = service.getOne(wrapper);
        if (existEntity != null) {
            throw new BusinessException(fieldName + "已存在");
        }
    }
    
    /**
     * 验证组合字段的唯一性（例如：班级代码在同一专业内唯一）
     * 
     * @param service Service实例
     * @param fieldGetter 字段getter方法引用
     * @param fieldValue 字段值
     * @param scopeGetter 范围字段getter方法引用（例如：专业ID）
     * @param scopeValue 范围字段值
     * @param deleteFlagGetter 删除标志字段getter方法引用
     * @param fieldName 字段名称（用于错误消息）
     * @param scopeName 范围字段名称（用于错误消息）
     * @param <T> 实体类型
     * @param <R> 字段类型
     * @param <S> 范围字段类型
     * @throws BusinessException 如果组合字段值已存在
     */
    public static <T, R, S> void validateUniqueInScope(IService<T> service,
                                                        SFunction<T, R> fieldGetter,
                                                        R fieldValue,
                                                        SFunction<T, S> scopeGetter,
                                                        S scopeValue,
                                                        SFunction<T, Integer> deleteFlagGetter,
                                                        String fieldName,
                                                        String scopeName) {
        if (fieldValue == null || scopeValue == null) {
            return;
        }
        
        LambdaQueryWrapper<T> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(fieldGetter, fieldValue)
               .eq(scopeGetter, scopeValue)
               .eq(deleteFlagGetter, DeleteFlag.NORMAL.getCode());
        
        T existEntity = service.getOne(wrapper);
        if (existEntity != null) {
            throw new BusinessException("该" + scopeName + "下" + fieldName + "已存在");
        }
    }
}

