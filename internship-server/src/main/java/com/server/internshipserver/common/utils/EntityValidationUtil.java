package com.server.internshipserver.common.utils;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.exception.BusinessException;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

/**
 * 实体验证工具类
 * 用于封装实体存在性和删除标志检查等重复逻辑
 */
public class EntityValidationUtil {
    
    /**
     * 检查实体是否存在且未删除
     * @param entity 实体对象
     * @param entityName 实体名称（用于错误消息）
     * @param <T> 实体类型
     * @throws BusinessException 如果实体为null或已删除
     */
    public static <T> void validateEntityExists(T entity, String entityName) {
        if (entity == null) {
            throw new BusinessException(entityName + "不存在");
        }
        
        // 检查删除标志
        try {
            Method getDeleteFlagMethod = entity.getClass().getMethod("getDeleteFlag");
            Integer deleteFlag = (Integer) getDeleteFlagMethod.invoke(entity);
            if (deleteFlag != null && deleteFlag.equals(DeleteFlag.DELETED.getCode())) {
                throw new BusinessException(entityName + "不存在");
            }
        } catch (Exception e) {
            // 如果实体没有deleteFlag字段，忽略删除标志检查
            // 这种情况不应该发生，但为了健壮性，我们捕获异常
        }
    }
    
    /**
     * 检查ID是否为空
     * @param id ID值
     * @param idName ID名称（用于错误消息）
     * @throws BusinessException 如果ID为null
     */
    public static void validateIdNotNull(Long id, String idName) {
        if (id == null) {
            throw new BusinessException(idName + "不能为空");
        }
    }
    
    /**
     * 检查实体是否存在且未删除（重载方法，使用默认实体名称）
     * @param entity 实体对象
     * @param <T> 实体类型
     * @throws BusinessException 如果实体为null或已删除
     */
    public static <T> void validateEntityExists(T entity) {
        validateEntityExists(entity, "实体");
    }
    
    /**
     * 检查实体的状态是否等于指定值
     * @param entity 实体对象
     * @param expectedStatus 期望的状态值
     * @param entityName 实体名称（用于错误消息）
     * @param errorMessage 错误消息（如果状态不匹配）
     * @param <T> 实体类型
     * @throws BusinessException 如果状态为null或不等于期望值
     */
    public static <T> void validateStatusEquals(T entity, Integer expectedStatus, String entityName, String errorMessage) {
        if (entity == null) {
            throw new BusinessException(entityName + "不存在");
        }
        
        try {
            Method getStatusMethod = entity.getClass().getMethod("getStatus");
            Integer status = (Integer) getStatusMethod.invoke(entity);
            if (status == null || !status.equals(expectedStatus)) {
                throw new BusinessException(errorMessage);
            }
        } catch (Exception e) {
            // 如果实体没有status字段，抛出异常
            throw new BusinessException("实体类型不支持状态检查");
        }
    }
    
    /**
     * 检查实体的状态是否等于指定值（使用默认错误消息）
     * @param entity 实体对象
     * @param expectedStatus 期望的状态值
     * @param entityName 实体名称（用于错误消息）
     * @param <T> 实体类型
     * @throws BusinessException 如果状态为null或不等于期望值
     */
    public static <T> void validateStatusEquals(T entity, Integer expectedStatus, String entityName) {
        validateStatusEquals(entity, expectedStatus, entityName, entityName + "状态不正确");
    }
    
    /**
     * 检查集合是否非空
     * @param collection 集合对象
     * @return 如果集合不为null且不为空，返回true；否则返回false
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }
    
    /**
     * 检查集合是否为空
     * @param collection 集合对象
     * @return 如果集合为null或为空，返回true；否则返回false
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
    
    /**
     * 检查分页结果的记录是否非空
     * @param page 分页结果对象
     * @return 如果分页结果不为null且记录不为空，返回true；否则返回false
     */
    public static <T> boolean hasRecords(Page<T> page) {
        return page != null && isNotEmpty(page.getRecords());
    }
    
    /**
     * 验证字符串非空
     * @param value 字符串值
     * @param fieldName 字段名称（用于错误消息）
     * @throws BusinessException 如果字符串为空或null
     */
    public static void validateStringNotBlank(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(fieldName + "不能为空");
        }
    }
    
    /**
     * 检查实体的状态是否不等于指定值
     * @param entity 实体对象
     * @param forbiddenStatus 禁止的状态值
     * @param entityName 实体名称（用于错误消息）
     * @param errorMessage 错误消息
     * @param <T> 实体类型
     * @throws BusinessException 如果状态等于禁止值
     */
    public static <T> void validateStatusNotEquals(T entity, Integer forbiddenStatus, String entityName, String errorMessage) {
        if (entity == null) {
            throw new BusinessException(entityName + "不存在");
        }
        
        try {
            Method getStatusMethod = entity.getClass().getMethod("getStatus");
            Integer status = (Integer) getStatusMethod.invoke(entity);
            if (status != null && status.equals(forbiddenStatus)) {
                throw new BusinessException(errorMessage);
            }
        } catch (Exception e) {
            throw new BusinessException("实体类型不支持状态检查");
        }
    }
    
    /**
     * 检查实体的状态是否在允许的列表中
     * @param entity 实体对象
     * @param allowedStatuses 允许的状态值列表
     * @param entityName 实体名称（用于错误消息）
     * @param errorMessage 错误消息
     * @param <T> 实体类型
     * @throws BusinessException 如果状态不在允许列表中
     */
    public static <T> void validateStatusIn(T entity, List<Integer> allowedStatuses, String entityName, String errorMessage) {
        if (entity == null) {
            throw new BusinessException(entityName + "不存在");
        }
        
        if (isEmpty(allowedStatuses)) {
            throw new BusinessException("允许的状态列表不能为空");
        }
        
        try {
            Method getStatusMethod = entity.getClass().getMethod("getStatus");
            Integer status = (Integer) getStatusMethod.invoke(entity);
            if (status == null || !allowedStatuses.contains(status)) {
                throw new BusinessException(errorMessage);
            }
        } catch (Exception e) {
            throw new BusinessException("实体类型不支持状态检查");
        }
    }
}

