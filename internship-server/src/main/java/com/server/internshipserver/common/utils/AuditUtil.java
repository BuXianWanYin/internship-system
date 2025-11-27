package com.server.internshipserver.common.utils;

import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.mapper.user.UserMapper;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 审核工具类
 * 用于封装设置审核信息等重复逻辑
 */
public class AuditUtil {
    
    private AuditUtil() {
        // 工具类，禁止实例化
    }
    
    /**
     * 设置审核信息（通用方法）
     * @param entity 实体对象
     * @param auditStatus 审核状态
     * @param auditOpinion 审核意见
     * @param auditorId 审核人ID
     * @param <T> 实体类型
     */
    public static <T> void setAuditInfo(T entity, Integer auditStatus, String auditOpinion, Long auditorId) {
        if (entity == null) {
            return;
        }
        
        try {
            Class<?> clazz = entity.getClass();
            
            // 设置审核状态（如果实体有status字段）
            try {
                Method setStatusMethod = clazz.getMethod("setStatus", Integer.class);
                setStatusMethod.invoke(entity, auditStatus);
            } catch (NoSuchMethodException e) {
                // 如果没有status字段，尝试设置auditStatus
                try {
                    Method setAuditStatusMethod = clazz.getMethod("setAuditStatus", Integer.class);
                    setAuditStatusMethod.invoke(entity, auditStatus);
                } catch (NoSuchMethodException ex) {
                    // 如果都没有，忽略
                }
            }
            
            // 设置审核时间
            try {
                Method setAuditTimeMethod = clazz.getMethod("setAuditTime", LocalDateTime.class);
                setAuditTimeMethod.invoke(entity, LocalDateTime.now());
            } catch (NoSuchMethodException e) {
                // 如果没有auditTime字段，忽略
            }
            
            // 设置审核意见
            try {
                Method setAuditOpinionMethod = clazz.getMethod("setAuditOpinion", String.class);
                setAuditOpinionMethod.invoke(entity, auditOpinion);
            } catch (NoSuchMethodException e) {
                // 如果没有auditOpinion字段，忽略
            }
            
            // 设置审核人ID
            if (auditorId != null) {
                try {
                    Method setAuditUserIdMethod = clazz.getMethod("setAuditUserId", Long.class);
                    setAuditUserIdMethod.invoke(entity, auditorId);
                } catch (NoSuchMethodException e) {
                    // 如果没有auditUserId字段，尝试auditorId
                    try {
                        Method setAuditorIdMethod = clazz.getMethod("setAuditorId", Long.class);
                        setAuditorIdMethod.invoke(entity, auditorId);
                    } catch (NoSuchMethodException ex) {
                        // 如果都没有，忽略
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("设置审核信息失败", e);
        }
    }
    
    /**
     * 设置审核信息（自动获取当前用户作为审核人）
     * @param entity 实体对象
     * @param auditStatus 审核状态
     * @param auditOpinion 审核意见
     * @param userMapper 用户Mapper（用于获取当前用户）
     * @param <T> 实体类型
     */
    public static <T> void setAuditInfo(T entity, Integer auditStatus, String auditOpinion, UserMapper userMapper) {
        UserInfo user = UserUtil.getCurrentUserOrNull(userMapper);
        Long auditorId = user != null ? user.getUserId() : null;
        setAuditInfo(entity, auditStatus, auditOpinion, auditorId);
    }
}

