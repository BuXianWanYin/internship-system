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
 * 专业信息实体类
 */
@ApiModel(description = "专业信息")
@Data
@TableName("major_info")
public class Major implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "专业ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long majorId;
    
    @ApiModelProperty(value = "专业名称", required = true, example = "计算机科学与技术")
    @TableField("major_name")
    private String majorName;
    
    @ApiModelProperty(value = "专业代码", required = true, example = "CS0101")
    @TableField("major_code")
    private String majorCode;
    
    @ApiModelProperty(value = "所属学院ID", required = true, example = "1")
    @TableField("college_id")
    private Long collegeId;
    
    @ApiModelProperty(value = "学制年限", example = "4")
    @TableField("duration")
    private Integer duration;
    
    @ApiModelProperty(value = "培养目标")
    @TableField("training_objective")
    private String trainingObjective;
    
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

