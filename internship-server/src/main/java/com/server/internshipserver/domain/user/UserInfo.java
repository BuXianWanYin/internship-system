package com.server.internshipserver.domain.user;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户基础信息实体类
 * 用于存储系统中所有用户的基础信息，包括用户名、密码、真实姓名、联系方式等
 * 支持多种角色，通过关联表实现用户与角色的多对多关系
 */
@ApiModel(description = "用户基础信息")
@Data
@TableName("user_info")
public class UserInfo implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "用户ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long userId;
    
    @ApiModelProperty(value = "用户名", required = true, example = "admin")
    @TableField("username")
    private String username;
    
    @ApiModelProperty(value = "密码（BCrypt加密）", required = true, hidden = true)
    @TableField("password")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)  // 只允许写入（反序列化），不允许读取（序列化），防止泄露
    private String password;
    
    @ApiModelProperty(value = "真实姓名", required = true, example = "张三")
    @TableField("real_name")
    private String realName;
    
    @ApiModelProperty(value = "身份证号", example = "110101199001011234")
    @TableField("id_card")
    private String idCard;
    
    @ApiModelProperty(value = "手机号", example = "13800000001")
    @TableField("phone")
    private String phone;
    
    @ApiModelProperty(value = "邮箱", example = "user@example.com")
    @TableField("email")
    private String email;
    
    @ApiModelProperty(value = "性别", example = "男")
    @TableField("gender")
    private String gender;
    
    @ApiModelProperty(value = "昵称", example = "昵称")
    @TableField("nickname")
    private String nickname;
    
    @ApiModelProperty(value = "头像URL", example = "/avatar/default.jpg")
    @TableField("avatar")
    private String avatar;
    
    @ApiModelProperty(value = "地址", example = "北京市朝阳区")
    @TableField("address")
    private String address;
    
    @ApiModelProperty(value = "个人介绍", example = "个人介绍")
    @TableField("introduction")
    private String introduction;
    
    @ApiModelProperty(value = "状态：1-启用，0-禁用", example = "1")
    @TableField("status")
    private Integer status;
    
    @ApiModelProperty(value = "删除标志：0-未删除，1-已删除", example = "0")
    @TableField("delete_flag")
    @TableLogic(value = "0", delval = "1")
    private Integer deleteFlag;
    
    @ApiModelProperty(value = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    @ApiModelProperty(value = "更新时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    
    @ApiModelProperty(value = "用户角色代码列表（非数据库字段）")
    @TableField(exist = false)
    private List<String> roles;
    
    @ApiModelProperty(value = "状态文字（非数据库字段，用于导出）")
    @TableField(exist = false)
    private String statusText;
    
    @ApiModelProperty(value = "创建时间文字（非数据库字段，用于导出）")
    @TableField(exist = false)
    private String createTimeText;
    
    @ApiModelProperty(value = "角色名称（非数据库字段，用于导出）")
    @TableField(exist = false)
    private String roleNames;
    
    // 角色化字段（非数据库字段，用于添加用户时传递数据）
    @ApiModelProperty(value = "学号（学生角色）")
    @TableField(exist = false)
    private String studentNo;
    
    @ApiModelProperty(value = "学校ID")
    @TableField(exist = false)
    private Long schoolId;
    
    @ApiModelProperty(value = "学院ID")
    @TableField(exist = false)
    private Long collegeId;
    
    @ApiModelProperty(value = "专业ID")
    @TableField(exist = false)
    private Long majorId;
    
    @ApiModelProperty(value = "班级ID（学生角色）")
    @TableField(exist = false)
    private Long classId;
    
    @ApiModelProperty(value = "班级ID列表（班主任角色，可管理多个班级）")
    @TableField(exist = false)
    private List<Long> classIds;
    
    @ApiModelProperty(value = "入学年份（学生角色）")
    @TableField(exist = false)
    private Integer enrollmentYear;
    
    @ApiModelProperty(value = "企业ID（企业管理员和企业导师角色）")
    @TableField(exist = false)
    private Long enterpriseId;
}

