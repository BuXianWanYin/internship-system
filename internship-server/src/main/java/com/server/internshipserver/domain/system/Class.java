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
 * 班级信息实体类
 */
@ApiModel(description = "班级信息")
@Data
@TableName("class_info")
public class Class implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "班级ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long classId;
    
    @ApiModelProperty(value = "班级名称", required = true, example = "计算机科学与技术2021级1班")
    @TableField("class_name")
    private String className;
    
    @ApiModelProperty(value = "班级代码", required = true, example = "CS202101")
    @TableField("class_code")
    private String classCode;
    
    @ApiModelProperty(value = "所属专业ID", required = true, example = "1")
    @TableField("major_id")
    private Long majorId;
    
    @ApiModelProperty(value = "入学年份", required = true, example = "2021")
    @TableField("enrollment_year")
    private Integer enrollmentYear;
    
    @ApiModelProperty(value = "班主任ID", example = "1")
    @TableField("class_teacher_id")
    private Long classTeacherId;
    
    @ApiModelProperty(value = "班级分享码", example = "A8B9C2D1")
    @TableField("share_code")
    private String shareCode;
    
    @ApiModelProperty(value = "分享码过期时间")
    @TableField("share_code_expire_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime shareCodeExpireTime;
    
    @ApiModelProperty(value = "分享码生成时间")
    @TableField("share_code_generate_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime shareCodeGenerateTime;
    
    @ApiModelProperty(value = "分享码使用次数", example = "0")
    @TableField("share_code_use_count")
    private Integer shareCodeUseCount;
    
    @ApiModelProperty(value = "状态：1-启用，0-禁用", example = "1")
    @TableField("status")
    private Integer status;
    
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

