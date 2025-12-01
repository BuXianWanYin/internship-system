package com.server.internshipserver.domain.internship;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 考勤组时间段实体类
 * 用于存储考勤组的工作时间段配置，包括时间段名称、上班时间、下班时间、工作时长等
 * 一个考勤组可以有多个时间段，支持早班、晚班、标准班等多种时间段配置
 */
@ApiModel(description = "考勤组时间段信息")
@Data
@TableName("attendance_group_time_slot")
public class AttendanceGroupTimeSlot implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "时间段ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long slotId;
    
    @ApiModelProperty(value = "考勤组ID", required = true, example = "1")
    @TableField("group_id")
    private Long groupId;
    
    @ApiModelProperty(value = "时间段名称（如：早班、晚班、标准班）", required = true, example = "标准班")
    @TableField("slot_name")
    private String slotName;
    
    @ApiModelProperty(value = "上班时间", required = true, example = "09:00:00")
    @TableField("start_time")
    @JsonFormat(pattern = "HH:mm")
    @JsonDeserialize(using = LocalTimeDeserializer.class)
    private LocalTime startTime;
    
    @ApiModelProperty(value = "下班时间", required = true, example = "18:00:00")
    @TableField("end_time")
    @JsonFormat(pattern = "HH:mm")
    @JsonDeserialize(using = LocalTimeDeserializer.class)
    private LocalTime endTime;
    
    /**
     * LocalTime 自定义反序列化器，支持 "HH:mm" 和 "HH:mm:ss" 两种格式
     */
    public static class LocalTimeDeserializer extends JsonDeserializer<LocalTime> {
        private static final DateTimeFormatter FORMATTER_HHMM = DateTimeFormatter.ofPattern("HH:mm");
        private static final DateTimeFormatter FORMATTER_HHMMSS = DateTimeFormatter.ofPattern("HH:mm:ss");
        
        @Override
        public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String timeStr = p.getText().trim();
            if (timeStr == null || timeStr.isEmpty()) {
                return null;
            }
            
            // 先尝试 "HH:mm:ss" 格式
            try {
                return LocalTime.parse(timeStr, FORMATTER_HHMMSS);
            } catch (DateTimeParseException e) {
                // 如果失败，尝试 "HH:mm" 格式
                try {
                    return LocalTime.parse(timeStr, FORMATTER_HHMM);
                } catch (DateTimeParseException e2) {
                    throw new IOException("无法解析时间格式: " + timeStr + "，支持的格式: HH:mm 或 HH:mm:ss", e2);
                }
            }
        }
    }
    
    @ApiModelProperty(value = "工作时长（小时）", required = true, example = "8.00")
    @TableField("work_hours")
    private BigDecimal workHours;
    
    @ApiModelProperty(value = "是否默认：1-是，0-否", example = "1")
    @TableField("is_default")
    private Integer isDefault;
    
    @ApiModelProperty(value = "排序顺序", example = "0")
    @TableField("sort_order")
    private Integer sortOrder;
    
    @ApiModelProperty(value = "删除标志：0-未删除，1-已删除", example = "0")
    @TableField("delete_flag")
    private Integer deleteFlag;
    
    @ApiModelProperty(value = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    @ApiModelProperty(value = "更新时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}

