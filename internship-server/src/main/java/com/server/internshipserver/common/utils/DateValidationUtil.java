package com.server.internshipserver.common.utils;

import com.server.internshipserver.common.exception.BusinessException;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 日期验证工具类
 * 提供日期和日期时间范围验证的方法，确保日期逻辑的正确性
 */
public final class DateValidationUtil {
    
    private DateValidationUtil() {
        // 工具类，禁止实例化
    }
    
    /**
     * 验证日期范围（开始日期不能晚于结束日期）
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param entityName 实体名称（用于错误消息）
     * @throws BusinessException 如果开始日期晚于结束日期
     */
    public static void validateDateRange(LocalDate startDate, LocalDate endDate, String entityName) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BusinessException(entityName + "开始日期不能晚于结束日期");
        }
    }
    
    /**
     * 验证日期时间范围（开始时间不能晚于结束时间）
     * 
     * @param startDateTime 开始时间
     * @param endDateTime 结束时间
     * @param entityName 实体名称（用于错误消息）
     * @throws BusinessException 如果开始时间晚于结束时间
     */
    public static void validateDateTimeRange(LocalDateTime startDateTime, LocalDateTime endDateTime, String entityName) {
        if (startDateTime != null && endDateTime != null && startDateTime.isAfter(endDateTime)) {
            throw new BusinessException(entityName + "开始时间不能晚于结束时间");
        }
    }
    
    /**
     * 验证日期是否在指定范围内
     * 
     * @param date 要验证的日期
     * @param minDate 最小日期（可以为null，表示不限制）
     * @param maxDate 最大日期（可以为null，表示不限制）
     * @param fieldName 字段名称（用于错误消息）
     * @throws BusinessException 如果日期不在范围内
     */
    public static void validateDateInRange(LocalDate date, LocalDate minDate, LocalDate maxDate, String fieldName) {
        if (date == null) {
            return;
        }
        
        if (minDate != null && date.isBefore(minDate)) {
            throw new BusinessException(fieldName + "不能早于" + minDate);
        }
        
        if (maxDate != null && date.isAfter(maxDate)) {
            throw new BusinessException(fieldName + "不能晚于" + maxDate);
        }
    }
    
    /**
     * 验证日期时间是否在指定范围内
     * 
     * @param dateTime 要验证的日期时间
     * @param minDateTime 最小日期时间（可以为null，表示不限制）
     * @param maxDateTime 最大日期时间（可以为null，表示不限制）
     * @param fieldName 字段名称（用于错误消息）
     * @throws BusinessException 如果日期时间不在范围内
     */
    public static void validateDateTimeInRange(LocalDateTime dateTime, LocalDateTime minDateTime, LocalDateTime maxDateTime, String fieldName) {
        if (dateTime == null) {
            return;
        }
        
        if (minDateTime != null && dateTime.isBefore(minDateTime)) {
            throw new BusinessException(fieldName + "不能早于" + minDateTime);
        }
        
        if (maxDateTime != null && dateTime.isAfter(maxDateTime)) {
            throw new BusinessException(fieldName + "不能晚于" + maxDateTime);
        }
    }
}

