package com.server.internshipserver.domain.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统配置实体类
 * 用于存储系统的配置信息，包括配置键、配置值、配置类型等
 * 支持动态配置系统参数，如实习类型、系统设置等
 */
@ApiModel(description = "系统配置")
@Data
@TableName("system_config")
public class SystemConfig implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "配置ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long configId;
    
    @ApiModelProperty(value = "配置键", required = true, example = "internship_type_production")
    @TableField("config_key")
    private String configKey;
    
    @ApiModelProperty(value = "配置值", example = "生产实习")
    @TableField("config_value")
    private String configValue;
    
    @ApiModelProperty(value = "配置类型", example = "internship_type")
    @TableField("config_type")
    private String configType;
    
    @ApiModelProperty(value = "配置描述", example = "生产实习类型")
    @TableField("description")
    private String description;
    
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

