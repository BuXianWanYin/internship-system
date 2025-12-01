# 方案C：实习结束标记功能实现文档

## 一、方案概述

### 1.1 方案选择
采用**方案C：双方都可以标记，企业优先**，区分合作企业实习和自主实习。

### 1.2 核心原则
- **合作企业实习**：企业优先标记，班主任可以容错标记
- **自主实习**：班主任标记（无企业参与）
- **状态联动**：标记后自动更新学生实习状态和相关状态

### 1.3 页面集成策略
**不新建页面**，在现有页面中添加标记功能：
1. **班主任**：在"实习申请审核"页面添加"结束实习"按钮
2. **企业管理员**：在"实习学生管理"页面添加"结束实习"按钮
3. **企业导师**：在"我指导的学生"页面添加"结束实习"按钮
4. **企业导师/管理员**：在"学生评价"页面添加"结束实习"按钮
5. **学生**：在"我的实习"页面显示实习结束状态

---

## 二、页面集成位置

### 2.1 班主任页面：实习申请审核页面

**文件位置：** `internship-web/src/views/admin/InternshipApplyAudit.vue`

**集成位置：**
1. **列表操作列**：在"解绑企业"按钮旁边添加"结束实习"按钮
2. **详情对话框**：在详情对话框的操作按钮区域添加"结束实习"按钮

**显示条件：**
- 合作企业：status=3（已录用）且未标记结束（status!=7）
- 自主实习：status=11（实习中）且未标记结束（status!=13）

**按钮位置：**
```vue
<!-- 在操作列中添加 -->
<el-button
  v-if="canMarkAsCompleted(row)"
  link
  type="success"
  size="small"
  @click="handleMarkAsCompleted(row)"
>
  结束实习
</el-button>
```

### 2.2 企业管理员页面：实习学生管理页面

**文件位置：** `internship-web/src/views/enterprise/StudentManagement.vue`

**集成位置：**
1. **列表操作列**：在"分配导师"按钮旁边添加"结束实习"按钮
2. **详情对话框**：在详情对话框的操作按钮区域添加"结束实习"按钮

**显示条件：**
- 仅合作企业：status=3（已录用）且未标记结束（status!=7）
- 不能标记已解绑的学生（unbindStatus!=2）
- 企业管理员只能标记本企业的学生

**按钮位置：**
```vue
<!-- 在操作列中添加 -->
<el-button
  v-if="canMarkAsCompleted(row)"
  link
  type="success"
  size="small"
  @click="handleMarkAsCompleted(row)"
>
  结束实习
</el-button>
```

### 2.3 企业导师页面：我指导的学生页面

**文件位置：** `internship-web/src/views/enterprise/MentorStudentManagement.vue`

**集成位置：**
1. **列表操作列**：在"分配考勤组"按钮旁边添加"结束实习"按钮
2. **详情对话框**：在详情对话框的操作按钮区域添加"结束实习"按钮

**显示条件：**
- 仅合作企业：status=3（已录用）且未标记结束（status!=7）
- 不能标记已解绑的学生（unbindStatus!=2）
- 企业导师只能标记分配给自己的学生

**按钮位置：**
```vue
<!-- 在操作列中添加 -->
<el-button
  v-if="canMarkAsCompleted(row)"
  link
  type="success"
  size="small"
  @click="handleMarkAsCompleted(row)"
>
  结束实习
</el-button>
```

### 2.4 企业页面：学生评价页面

**文件位置：** `internship-web/src/views/enterprise/StudentEvaluation.vue`

**集成位置：**
1. **列表操作列**：在"评价"按钮旁边添加"结束实习"按钮
2. **评价对话框**：在评价对话框的操作按钮区域添加"结束实习"按钮

**显示条件：**
- 仅合作企业：status=3（已录用）且未标记结束（status!=7）
- 企业导师/管理员只能标记本企业的学生

**按钮位置：**
```vue
<!-- 在操作列中添加 -->
<el-button
  v-if="canMarkAsCompleted(row)"
  link
  type="success"
  size="small"
  @click="handleMarkAsCompleted(row)"
>
  结束实习
</el-button>
```

### 2.5 学生页面：我的实习页面

**文件位置：** `internship-web/src/views/student/MyInternship.vue`

**集成位置：**
- **状态显示**：显示实习结束状态（如果已标记）
- **信息展示**：显示实习结束日期和备注

**显示内容：**
- 如果实习已结束，显示"实习已结束"标签
- 显示实习结束日期
- 显示结束备注（如果有）

---

## 三、权限设计

### 3.1 合作企业实习

**可以标记的角色（优先级从高到低）：**
1. **企业管理员**（ROLE_ENTERPRISE_ADMIN）- 最高优先级
2. **企业导师**（ROLE_ENTERPRISE_MENTOR）- 最高优先级
3. **班主任**（ROLE_CLASS_TEACHER）- 次优先级（容错）
4. **系统管理员**（ROLE_SYSTEM_ADMIN）- 兜底

**标记规则：**
- 企业标记后，班主任不能再标记
- 班主任标记后，企业可以覆盖（需要确认）
- 系统管理员可以随时标记

### 3.2 自主实习

**可以标记的角色：**
1. **班主任**（ROLE_CLASS_TEACHER）
2. **学校管理员**（ROLE_SCHOOL_ADMIN）
3. **学院负责人**（ROLE_COLLEGE_LEADER）
4. **系统管理员**（ROLE_SYSTEM_ADMIN）- 兜底

**标记规则：**
- 只有学校相关角色可以标记
- 企业角色无法标记（自主实习没有企业）

---

## 四、状态联动设计

### 4.1 状态联动完整清单

**结束实习时需要联动更新的所有状态字段清单：**

| 序号 | 表名 | 字段名 | 字段说明 | 操作类型 | 更新规则 | 备注 |
|------|------|--------|----------|----------|----------|------|
| 1 | `internship_apply` | `status` | 申请状态 | **必须更新** | 合作企业：3→7<br>自主实习：11→13 | 核心状态 |
| 2 | `internship_apply` | `internship_end_date` | 实习结束日期 | **必须更新** | 传入日期或当前日期 | 记录结束时间 |
| 3 | `internship_apply` | `unbind_audit_opinion` | 备注字段 | **可选更新** | 存储结束备注 | 复用字段 |
| 4 | `student_info` | `internship_status` | 学生实习状态 | **必须更新** | 1（实习中）→3（已结束） | 核心状态 |
| 5 | `internship_apply` | `unbind_status` | 解绑状态（在职/离职） | **保持不变** | 保持原值 | 已解绑不能结束，结束后不更新（通过status区分） |
| 6 | `student_info` | `current_apply_id` | 当前申请ID | **保持不变** | 保持原值 | 历史记录 |
| 7 | `student_info` | `current_enterprise_id` | 当前企业ID | **保持不变** | 保持原值 | 历史记录 |

### 4.2 申请状态更新（InternshipApply.status）

**合作企业实习：**
- 标记前：status=3（已录用）
- 标记后：status=7（实习结束）

**自主实习：**
- 标记前：status=11（实习中）
- 标记后：status=13（实习结束）

**实现位置：** `updateApplyForComplete` 方法

### 4.3 实习结束日期更新（InternshipApply.internshipEndDate）

**更新规则：**
- 如果传入了结束日期，使用传入的日期
- 如果没有传入，使用当前日期（LocalDate.now()）
- **验证：** 结束日期不能早于开始日期（internshipStartDate）

**实现位置：** `updateApplyForComplete` 方法

### 4.4 学生实习状态更新（Student.internshipStatus）

**必须更新：** `Student.internshipStatus`

**更新逻辑：**
- 标记前：internshipStatus=1（实习中）
- 标记后：internshipStatus=3（已结束）

**更新时机：**
- 结束实习时立即更新
- 确保学生状态与申请状态一致

**实现位置：** `updateStudentAfterComplete` 方法

### 4.5 备注字段更新（InternshipApply.unbindAuditOpinion）

**更新规则：**
- 如果传入了备注，保存到 `unbindAuditOpinion` 字段
- 如果没有传入，不更新该字段（保持原值）

**注意：** 复用现有字段，用于存储结束实习的备注信息

**实现位置：** `updateApplyForComplete` 方法

### 4.6 在职状态联动（InternshipApply.unbindStatus）

**字段说明：** `InternshipApply.unbindStatus` - 解绑状态（在职/离职状态）

**UnbindStatus 枚举值：**
- 0: 未解绑（在职）
- 1: 已申请解绑（待企业管理员审批）
- 2: 已解绑（离职审批通过）
- 3: 已拒绝
- 4: 企业管理员已审批（待学校审批）

**业务逻辑分析：**
- **解绑**：学生提前离职，需要走解绑审批流程
- **实习结束**：正常完成实习周期，学生离开企业

**⚠️ 重要问题：如何区分"提前离职"和"实习结束"？**

当前 `unbindStatus=2` 可能表示两种情况：
1. **提前离职（解绑）**：`status=3`（已录用）+ `unbindStatus=2`（已解绑）
2. **实习结束**：`status=7`（合作企业）或 `status=13`（自主实习）+ `unbindStatus=2`（已解绑）

**解决方案：采用方案A（推荐）**

**方案A：不更新 `unbindStatus`，通过 `status` 区分（推荐）**

**联动规则：**

1. **检查规则（标记前）：**
   - 如果 `unbindStatus=2`（已解绑），说明已经提前离职，**不能标记为实习结束**
   - 如果 `unbindStatus=1`（已申请解绑）或 `4`（企业管理员已审批），说明正在解绑流程中，**可以标记为实习结束**（实习结束会覆盖解绑流程）

2. **更新规则（标记后）：**
   - **不更新 `unbindStatus`**：实习结束后，**保持 `unbindStatus` 原值不变**
   - **区分逻辑**：
     - **提前离职**：`status=3` + `unbindStatus=2` → 显示"已离职"
     - **实习结束**：`status=7` 或 `status=13` + `unbindStatus` 保持原值（可能是0、1、3、4等）→ 显示"实习结束"
   - **前端判断**：通过 `status` 字段即可判断，不需要组合判断 `unbindStatus`

**方案B：添加新字段 `internshipEndType`（可选，需要改表）**

如果需要更清晰的语义，可以添加新字段：

**数据库字段：** `internship_apply.internship_end_type`
- 0: 未结束
- 1: 正常结束（实习结束）
- 2: 提前离职（解绑）

**联动规则：**
- 实习结束时：设置 `internshipEndType=1`
- 解绑时：设置 `internshipEndType=2`
- 前端通过 `internshipEndType` 直接判断显示

**推荐使用方案A**，因为：
1. 不需要修改数据库结构
2. 通过 `status` 字段就能清晰区分
3. 逻辑简单，前端判断方便

**实现位置：** 
- 检查：`validateStatusForComplete` 方法
- 更新：`updateApplyForComplete` 方法（方案A：不更新 `unbindStatus`）

### 4.7 学生当前申请ID（Student.currentApplyId）

**不更新：** 结束实习后保持原值

**原因：** 保留历史记录，用于查询和统计

**实现位置：** `updateStudentAfterComplete` 方法（明确不更新）

### 4.8 学生当前企业ID（Student.currentEnterpriseId）

**不更新：** 结束实习后保持原值

**原因：** 保留历史记录，用于查询和统计

**实现位置：** `updateStudentAfterComplete` 方法（明确不更新）

### 4.9 状态联动检查清单

**在实现 `completeInternship` 方法时，必须确保以下所有状态都被正确处理：**

```java
// ✅ 1. 更新 InternshipApply.status
apply.setStatus(InternshipApplyStatus.COMPLETED.getCode()); // 或 SelfInternshipApplyStatus.COMPLETED

// ✅ 2. 更新 InternshipApply.internshipEndDate
apply.setInternshipEndDate(endDate != null ? endDate : LocalDate.now());

// ✅ 3. 更新 InternshipApply.unbindAuditOpinion（如果传入了备注）
if (remark != null && !remark.trim().isEmpty()) {
    apply.setUnbindAuditOpinion(remark);
}

// ✅ 4. 更新 Student.internshipStatus
student.setInternshipStatus(StudentInternshipStatus.COMPLETED.getCode());

// ✅ 5. 检查 InternshipApply.unbindStatus（不能是已解绑状态）
if (apply.getUnbindStatus() != null && apply.getUnbindStatus().equals(UnbindStatus.UNBOUND.getCode())) {
    throw new BusinessException("已解绑的实习申请不能标记为结束，已解绑表示学生已提前离职");
}

// ✅ 5.1 不更新 InternshipApply.unbindStatus（保持原值）
// 原因：通过 status 字段即可区分"提前离职"和"实习结束"
// - 提前离职：status=3 + unbindStatus=2 → 显示"已离职"
// - 实习结束：status=7 或 13 + unbindStatus 保持原值 → 显示"实习结束"

// ✅ 6. 不更新 Student.currentApplyId（保持原值）

// ✅ 7. 不更新 Student.currentEnterpriseId（保持原值）
```

### 4.10 状态联动验证要点

**在测试时，需要验证以下所有状态：**

1. ✅ `InternshipApply.status` 是否正确更新（3→7 或 11→13）
2. ✅ `InternshipApply.internshipEndDate` 是否正确设置
3. ✅ `InternshipApply.unbindAuditOpinion` 是否正确保存（如果有备注）
4. ✅ `Student.internshipStatus` 是否正确更新（1→3）
5. ✅ `InternshipApply.unbindStatus` 是否保持不变
6. ✅ `Student.currentApplyId` 是否保持不变
7. ✅ `Student.currentEnterpriseId` 是否保持不变
8. ✅ 已解绑的申请是否被正确拒绝

---

## 五、后端实现

### 5.1 Controller 层

**文件位置：** `InternshipApplyController.java`

```java
@ApiOperation("结束实习（单个）")
@PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER', 'ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR')")
@PostMapping("/{applyId}/complete")
public Result<?> completeInternship(
        @ApiParam(value = "申请ID", required = true) @PathVariable Long applyId,
        @ApiParam(value = "实习结束日期（可选，格式：yyyy-MM-dd）", required = false) 
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
        @ApiParam(value = "备注", required = false) @RequestParam(required = false) String remark) {
    internshipApplyService.completeInternship(applyId, endDate, remark);
    return Result.success("结束实习成功");
}

@ApiOperation("批量结束实习")
@PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER', 'ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR')")
@PostMapping("/batch-complete")
public Result<?> batchCompleteInternship(
        @ApiParam(value = "申请ID列表", required = true) @RequestParam List<Long> applyIds,
        @ApiParam(value = "实习结束日期（可选，格式：yyyy-MM-dd）", required = false) 
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
        @ApiParam(value = "备注", required = false) @RequestParam(required = false) String remark) {
    internshipApplyService.batchCompleteInternship(applyIds, endDate, remark);
    return Result.success("批量结束实习成功");
}
```

### 5.2 Service 接口层

**文件位置：** `InternshipApplyService.java`

```java
/**
 * 结束实习（单个）
 * @param applyId 申请ID
 * @param endDate 实习结束日期（可选，不传则使用当前日期）
 * @param remark 备注
 */
void completeInternship(Long applyId, LocalDate endDate, String remark);

/**
 * 批量结束实习
 * @param applyIds 申请ID列表
 * @param endDate 实习结束日期（可选，不传则使用当前日期）
 * @param remark 备注
 */
void batchCompleteInternship(List<Long> applyIds, LocalDate endDate, String remark);
```

### 5.3 Service 实现层

**文件位置：** `InternshipApplyServiceImpl.java`

**需要添加的依赖注入（如果还没有）：**
```java
@Autowired
private EnterpriseMentorMapper enterpriseMentorMapper;

@Autowired
private UserMapper userMapper;
```

**实现代码：**

```java
@Override
@Transactional(rollbackFor = Exception.class)
public void completeInternship(Long applyId, LocalDate endDate, String remark) {
    // 1. 参数校验
    EntityValidationUtil.validateIdNotNull(applyId, "申请ID");
    
    // 2. 查询申请信息
    InternshipApply apply = this.getById(applyId);
    EntityValidationUtil.validateEntityExists(apply, "申请");
    
    // 3. 权限检查（区分合作企业和自主实习）
    checkCompletePermission(apply);
    
    // 4. 状态检查：只有符合条件的状态才能标记为结束
    validateStatusForComplete(apply);
    
    // 5. 检查是否已标记
    if (isInternshipCompleted(apply)) {
        throw new BusinessException("该实习申请已经标记为结束");
    }
    
    // 6. 更新申请信息（更新 status、internshipEndDate、unbindAuditOpinion）
    updateApplyForComplete(apply, endDate, remark);
    
    // 7. 触发联动操作（更新 Student.internshipStatus，保持 currentApplyId 和 currentEnterpriseId 不变）
    triggerCompleteActions(apply);
    
    // 状态联动完成检查清单：
    // ✅ InternshipApply.status - 已更新（3→7 或 11→13）
    // ✅ InternshipApply.internshipEndDate - 已更新
    // ✅ InternshipApply.unbindAuditOpinion - 已更新（如果有备注）
    // ⚠️ InternshipApply.unbindStatus - 不更新（保持原值），通过 status 区分"提前离职"和"实习结束"
    // ✅ Student.internshipStatus - 已更新（1→3）
    // ✅ InternshipApply.unbindStatus - 已检查（不能是已解绑状态）
    // ✅ Student.currentApplyId - 保持不变
    // ✅ Student.currentEnterpriseId - 保持不变
}

/**
 * 权限检查（区分合作企业和自主实习）
 */
private void checkCompletePermission(InternshipApply apply) {
    Integer applyType = apply.getApplyType();
    
    if (applyType == null) {
        throw new BusinessException("申请类型不能为空");
    }
    
    // 系统管理员可以标记所有
    if (dataPermissionUtil.isSystemAdmin()) {
        return;
    }
    
    // 根据申请类型判断权限
    if (applyType.equals(ApplyType.COOPERATION.getCode())) {
        // 合作企业实习：检查企业权限或班主任权限
        checkCooperationPermission(apply);
    } else if (applyType.equals(ApplyType.SELF.getCode())) {
        // 自主实习：检查学校权限
        checkSelfInternshipPermission(apply);
    } else {
        throw new BusinessException("未知的申请类型");
    }
}

/**
 * 合作企业实习权限检查（方案C：企业优先，班主任容错）
 */
private void checkCooperationPermission(InternshipApply apply) {
    Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
    
    // 企业管理员可以标记本企业的申请（优先）
    if (dataPermissionUtil.hasRole(Constants.ROLE_ENTERPRISE_ADMIN)) {
        if (currentUserEnterpriseId != null && 
            apply.getEnterpriseId() != null &&
            currentUserEnterpriseId.equals(apply.getEnterpriseId())) {
            return;
        }
    }
    
    // 企业导师可以标记分配给自己的学生（优先）
    if (dataPermissionUtil.hasRole(Constants.ROLE_ENTERPRISE_MENTOR)) {
        if (currentUserEnterpriseId != null && 
            apply.getEnterpriseId() != null &&
            currentUserEnterpriseId.equals(apply.getEnterpriseId())) {
            // 检查是否分配给当前导师
            UserInfo currentUser = UserUtil.getCurrentUser(userMapper);
            if (currentUser != null) {
                // 查询当前用户对应的企业导师
                LambdaQueryWrapper<EnterpriseMentor> mentorWrapper = new LambdaQueryWrapper<>();
                mentorWrapper.eq(EnterpriseMentor::getUserId, currentUser.getUserId())
                            .eq(EnterpriseMentor::getDeleteFlag, DeleteFlag.NORMAL.getCode());
                EnterpriseMentor mentor = enterpriseMentorMapper.selectOne(mentorWrapper);
                
                if (mentor != null && mentor.getMentorId() != null) {
                    // 检查申请是否分配给该导师
                    if (apply.getMentorId() != null && apply.getMentorId().equals(mentor.getMentorId())) {
                        return;
                    }
                }
            }
        }
    }
    
    // 班主任可以标记本班学生的申请（容错）
    if (dataPermissionUtil.hasRole(Constants.ROLE_CLASS_TEACHER)) {
        if (apply.getStudentId() == null) {
            throw new BusinessException("无权标记该实习申请");
        }
        Student student = studentMapper.selectById(apply.getStudentId());
        if (student == null || student.getClassId() == null) {
            throw new BusinessException("无权标记该实习申请");
        }
        List<Long> classIds = dataPermissionUtil.getCurrentUserClassIds();
        if (classIds != null && classIds.contains(student.getClassId())) {
            // 检查是否已被企业标记
            if (isInternshipCompleted(apply)) {
                throw new BusinessException("该实习已被企业标记为结束，无需重复标记");
            }
            return;
        }
    }
    
    throw new BusinessException("无权标记该实习申请（仅企业管理员/企业导师/班主任可以标记）");
}

/**
 * 自主实习权限检查
 */
private void checkSelfInternshipPermission(InternshipApply apply) {
    // 学校管理员可以标记本校学生的申请
    if (dataPermissionUtil.hasRole(Constants.ROLE_SCHOOL_ADMIN)) {
        return;
    }
    
    // 学院负责人可以标记本院学生的申请
    if (dataPermissionUtil.hasRole(Constants.ROLE_COLLEGE_LEADER)) {
        if (apply.getStudentId() == null) {
            throw new BusinessException("无权标记该实习申请");
        }
        Student student = studentMapper.selectById(apply.getStudentId());
        if (student == null || student.getCollegeId() == null) {
            throw new BusinessException("无权标记该实习申请");
        }
        Long currentUserCollegeId = dataPermissionUtil.getCurrentUserCollegeId();
        if (currentUserCollegeId != null && currentUserCollegeId.equals(student.getCollegeId())) {
            return;
        }
    }
    
    // 班主任可以标记本班学生的申请
    if (dataPermissionUtil.hasRole(Constants.ROLE_CLASS_TEACHER)) {
        if (apply.getStudentId() == null) {
            throw new BusinessException("无权标记该实习申请");
        }
        Student student = studentMapper.selectById(apply.getStudentId());
        if (student == null || student.getClassId() == null) {
            throw new BusinessException("无权标记该实习申请");
        }
        List<Long> classIds = dataPermissionUtil.getCurrentUserClassIds();
        if (classIds != null && classIds.contains(student.getClassId())) {
            return;
        }
    }
    
    throw new BusinessException("无权标记该实习申请（仅班主任/学校管理员可以标记自主实习）");
}

/**
 * 状态验证：只有符合条件的状态才能标记为结束
 * 
 * 状态检查：
 * ✅ InternshipApply.unbindStatus - 检查是否为已解绑状态（已解绑不能结束）
 * ✅ InternshipApply.status - 检查当前状态是否符合要求
 */
private void validateStatusForComplete(InternshipApply apply) {
    Integer status = apply.getStatus();
    Integer applyType = apply.getApplyType();
    
    if (applyType == null) {
        throw new BusinessException("申请类型不能为空");
    }
    
    // 检查是否已解绑（已解绑说明已经提前离职，不能标记为实习结束）
    if (apply.getUnbindStatus() != null && apply.getUnbindStatus().equals(UnbindStatus.UNBOUND.getCode())) {
        throw new BusinessException("已解绑的实习申请不能标记为结束，已解绑表示学生已提前离职");
    }
    
    // 合作企业：只有"已录用"（status=3）才能标记为结束
    if (applyType.equals(ApplyType.COOPERATION.getCode())) {
        if (status == null || !status.equals(InternshipApplyStatus.ACCEPTED.getCode())) {
            throw new BusinessException("只有已录用的合作企业实习申请才能标记为结束");
        }
    }
    
    // 自主实习：只有"实习中"（status=11）才能标记为结束
    if (applyType.equals(ApplyType.SELF.getCode())) {
        if (status == null || !status.equals(SelfInternshipApplyStatus.IN_PROGRESS.getCode())) {
            throw new BusinessException("只有实习中的自主实习申请才能标记为结束");
        }
    }
}

/**
 * 更新申请信息
 * 
 * 状态联动：
 * ✅ InternshipApply.status - 更新为 7（合作企业）或 13（自主实习）
 * ✅ InternshipApply.internshipEndDate - 更新为传入日期或当前日期
 * ✅ InternshipApply.unbindAuditOpinion - 如果有备注则更新
 * ⚠️ InternshipApply.unbindStatus - 不更新（保持原值），通过 status 区分"提前离职"和"实习结束"
 */
private void updateApplyForComplete(InternshipApply apply, LocalDate endDate, String remark) {
    // 设置实习结束日期
    if (endDate != null) {
        // 验证结束日期不能早于开始日期
        LocalDate startDate = apply.getInternshipStartDate();
        if (startDate != null && endDate.isBefore(startDate)) {
            throw new BusinessException("实习结束日期不能早于开始日期");
        }
        apply.setInternshipEndDate(endDate);
    } else {
        // 如果没有传入结束日期，使用当前日期
        apply.setInternshipEndDate(LocalDate.now());
    }
    
    // 根据申请类型设置状态
    if (apply.getApplyType().equals(ApplyType.COOPERATION.getCode())) {
        // 合作企业：设置为实习结束（status=7）
        apply.setStatus(InternshipApplyStatus.COMPLETED.getCode());
    } else if (apply.getApplyType().equals(ApplyType.SELF.getCode())) {
        // 自主实习：设置为实习结束（status=13）
        apply.setStatus(SelfInternshipApplyStatus.COMPLETED.getCode());
    }
    
    // 保存备注（使用 unbindAuditOpinion 字段存储）
    if (remark != null && !remark.trim().isEmpty()) {
        apply.setUnbindAuditOpinion(remark);
    }
    
    // ⚠️ 注意：不更新 unbindStatus
    // 原因：通过 status 字段即可区分"提前离职"和"实习结束"
    // - 提前离职：status=3 + unbindStatus=2 → 显示"已离职"
    // - 实习结束：status=7 或 13 + unbindStatus 保持原值 → 显示"实习结束"
    // 如果更新 unbindStatus=2，会导致无法区分两种情况
    
    this.updateById(apply);
}

/**
 * 触发联动操作
 * 
 * 状态联动清单：
 * 1. ✅ InternshipApply.status - 已在 updateApplyForComplete 中更新
 * 2. ✅ InternshipApply.internshipEndDate - 已在 updateApplyForComplete 中更新
 * 3. ✅ InternshipApply.unbindAuditOpinion - 已在 updateApplyForComplete 中更新
 * 4. ⚠️ InternshipApply.unbindStatus - 不更新（保持原值），通过 status 区分"提前离职"和"实习结束"
 * 5. ✅ Student.internshipStatus - 在 updateStudentAfterComplete 中更新
 * 6. ✅ InternshipApply.unbindStatus - 已在 validateStatusForComplete 中检查（不能是已解绑）
 * 7. ✅ Student.currentApplyId - 在 updateStudentAfterComplete 中保持不变
 * 8. ✅ Student.currentEnterpriseId - 在 updateStudentAfterComplete 中保持不变
 */
private void triggerCompleteActions(InternshipApply apply) {
    // 1. 更新学生实习状态
    updateStudentAfterComplete(apply.getStudentId());
    
    // 2. 记录操作日志
    // logOperation(apply, "结束实习");
}

/**
 * 更新学生实习状态
 * 
 * 状态联动：
 * ✅ Student.internshipStatus - 更新为 3（已结束）
 * ✅ Student.currentApplyId - 保持不变（保留历史记录）
 * ✅ Student.currentEnterpriseId - 保持不变（保留历史记录）
 */
private void updateStudentAfterComplete(Long studentId) {
    if (studentId == null) {
        return;
    }
    
    Student student = studentMapper.selectById(studentId);
    if (student != null) {
        // 更新学生实习状态为"已结束"
        student.setInternshipStatus(StudentInternshipStatus.COMPLETED.getCode());
        // 注意：不修改 currentApplyId 和 currentEnterpriseId，保留历史记录
        // 明确不更新以下字段：
        // - currentApplyId: 保持原值，用于历史记录查询
        // - currentEnterpriseId: 保持原值，用于历史记录查询
        studentMapper.updateById(student);
    }
}

/**
 * 判断实习是否已结束
 */
private boolean isInternshipCompleted(InternshipApply apply) {
    if (apply == null || apply.getStatus() == null) {
        return false;
    }
    
    Integer status = apply.getStatus();
    Integer applyType = apply.getApplyType();
    
    // 合作企业：status=7
    if (applyType != null && applyType.equals(ApplyType.COOPERATION.getCode())) {
        return status.equals(InternshipApplyStatus.COMPLETED.getCode());
    }
    
    // 自主实习：status=13
    if (applyType != null && applyType.equals(ApplyType.SELF.getCode())) {
        return status.equals(SelfInternshipApplyStatus.COMPLETED.getCode());
    }
    
    return false;
}

@Override
@Transactional(rollbackFor = Exception.class)
public void batchCompleteInternship(List<Long> applyIds, LocalDate endDate, String remark) {
    if (applyIds == null || applyIds.isEmpty()) {
        throw new BusinessException("申请ID列表不能为空");
    }
    
    int successCount = 0;
    int failCount = 0;
    List<String> errorMessages = new ArrayList<>();
    
    for (Long applyId : applyIds) {
        try {
            completeInternship(applyId, endDate, remark);
            successCount++;
        } catch (Exception e) {
            failCount++;
            errorMessages.add("申请ID " + applyId + ": " + e.getMessage());
        }
    }
    
    if (failCount > 0) {
        String message = String.format("批量标记完成：成功 %d 条，失败 %d 条。", successCount, failCount);
        if (successCount > 0) {
            throw new BusinessException(message + "失败详情：" + String.join("; ", errorMessages));
        } else {
            throw new BusinessException("批量标记全部失败：" + String.join("; ", errorMessages));
        }
    }
}
```

---

## 六、前端实现

### 6.1 API 接口封装

**文件位置：** `internship-web/src/api/internship/apply.js`

**添加以下接口方法：**

```javascript
// 结束实习（单个）
completeInternship(applyId, endDate, remark) {
  return request.post(`/internship/apply/${applyId}/complete`, null, {
    params: {
      endDate: endDate ? formatDate(endDate, 'YYYY-MM-DD') : undefined,
      remark: remark || undefined
    }
  })
},

// 批量结束实习
batchCompleteInternship(applyIds, endDate, remark) {
  return request.post('/internship/apply/batch-complete', null, {
    params: {
      applyIds: applyIds.join(','),
      endDate: endDate ? formatDate(endDate, 'YYYY-MM-DD') : undefined,
      remark: remark || undefined
    }
  })
}
```

**注意：** 需要导入日期格式化工具函数（如果还没有）：
```javascript
import { formatDate } from '@/utils/dateUtils'
```

### 6.2 班主任页面集成

**文件位置：** `internship-web/src/views/admin/InternshipApplyAudit.vue`

**添加内容：**

```vue
<!-- 1. 在操作列添加按钮 -->
<el-table-column label="操作" width="300" fixed="right" align="center">
  <template #default="{ row }">
    <el-button link type="primary" size="small" @click="handleView(row)">查看详情</el-button>
    <el-button
      v-if="row.status === 0"
      link
      type="success"
      size="small"
      @click="handleAudit(row, 1)"
    >
      通过
    </el-button>
    <el-button
      v-if="row.status === 0"
      link
      type="danger"
      size="small"
      @click="handleAudit(row, 2)"
    >
      拒绝
    </el-button>
    <el-button
      v-if="canMarkAsCompleted(row)"
      link
      type="success"
      size="small"
      @click="handleMarkAsCompleted(row)"
    >
      结束实习
    </el-button>
    <el-button
      v-if="row.status === 3 && (row.unbindStatus === 0 || row.unbindStatus === null)"
      link
      type="danger"
      size="small"
      @click="handleUnbind(row)"
    >
      解绑企业
    </el-button>
  </template>
</el-table-column>

<!-- 2. 在详情对话框添加按钮 -->
<template #footer>
  <el-button @click="detailDialogVisible = false">关闭</el-button>
  <el-button
    v-if="detailData.status === 0"
    type="success"
    @click="handleAuditFromDetail(1)"
  >
    通过
  </el-button>
  <el-button
    v-if="detailData.status === 0"
    type="danger"
    @click="handleAuditFromDetail(2)"
  >
    拒绝
  </el-button>
  <el-button
    v-if="canMarkAsCompleted(detailData)"
    type="success"
    @click="handleMarkAsCompletedFromDetail"
  >
    结束实习
  </el-button>
</template>

<!-- 3. 添加结束实习对话框 -->
<el-dialog
  v-model="completeDialogVisible"
  title="结束实习"
  width="600px"
  :close-on-click-modal="false"
>
  <el-form
    ref="completeFormRef"
    :model="completeForm"
    :rules="completeFormRules"
    label-width="120px"
  >
    <el-form-item label="申请信息">
      <div style="padding: 10px; background: #f5f7fa; border-radius: 4px">
        <div><strong>学生：</strong>{{ currentCompleteApply.studentName }}（{{ currentCompleteApply.studentNo }}）</div>
        <div style="margin-top: 5px">
          <strong>企业：</strong>
          {{ currentCompleteApply.enterpriseName || currentCompleteApply.selfEnterpriseName || '-' }}
        </div>
        <div style="margin-top: 5px">
          <strong>岗位：</strong>
          {{ currentCompleteApply.postName || currentCompleteApply.selfPostName || '-' }}
        </div>
      </div>
    </el-form-item>
    <el-form-item label="实习结束日期" prop="endDate">
      <el-date-picker
        v-model="completeForm.endDate"
        type="date"
        placeholder="请选择结束日期（不选则使用今天）"
        value-format="YYYY-MM-DD"
        style="width: 100%"
      />
    </el-form-item>
    <el-form-item label="备注">
      <el-input
        v-model="completeForm.remark"
        type="textarea"
        :rows="4"
        placeholder="请输入备注"
        maxlength="500"
        show-word-limit
      />
    </el-form-item>
    <el-alert
      type="info"
      :closable="false"
      show-icon
      style="margin-top: 10px"
    >
      <template #default>
        <div>结束实习后，学生的实习状态将更新为"已结束"，可以进行评价。</div>
      </template>
    </el-alert>
  </el-form>
  <template #footer>
    <el-button @click="completeDialogVisible = false">取消</el-button>
    <el-button type="primary" :loading="completeLoading" @click="handleSubmitComplete">确定</el-button>
  </template>
</el-dialog>
```

**添加脚本：**

```javascript
// 添加响应式数据
const completeDialogVisible = ref(false)
const completeFormRef = ref(null)
const currentCompleteApply = ref({})
const completeLoading = ref(false)

const completeForm = reactive({
  endDate: null,
  remark: ''
})

const completeFormRules = {
  endDate: [
    {
      validator: (rule, value, callback) => {
        if (value) {
          const endDate = new Date(value)
          const startDate = currentCompleteApply.value.internshipStartDate || 
                           currentCompleteApply.value.selfStartDate
          if (startDate && endDate < new Date(startDate)) {
            callback(new Error('实习结束日期不能早于开始日期'))
          } else {
            callback()
          }
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

// 判断是否可以标记为结束
const canMarkAsCompleted = (row) => {
  // 合作企业：status=3（已录用）且未标记结束（status!=7）
  if (row.applyType === 1) {
    return row.status === 3 && row.status !== 7
  }
  // 自主实习：status=11（实习中）且未标记结束（status!=13）
  if (row.applyType === 2) {
    return row.status === 11 && row.status !== 13
  }
  return false
}

// 处理结束实习
const handleMarkAsCompleted = (row) => {
  currentCompleteApply.value = { ...row }
  completeForm.endDate = null
  completeForm.remark = ''
  completeDialogVisible.value = true
}

// 从详情对话框处理结束实习
const handleMarkAsCompletedFromDetail = () => {
  currentCompleteApply.value = { ...detailData.value }
  completeForm.endDate = null
  completeForm.remark = ''
  completeDialogVisible.value = true
}

// 提交结束实习
const handleSubmitComplete = async () => {
  if (!completeFormRef.value) return
  
  try {
    await completeFormRef.value.validate()
    
    completeLoading.value = true
    try {
      const res = await applyApi.completeInternship(
        currentCompleteApply.value.applyId,
        completeForm.endDate,
        completeForm.remark
      )
      if (res.code === 200) {
        ElMessage.success('结束实习成功')
        completeDialogVisible.value = false
        // 刷新列表
        await loadData()
        // 如果详情对话框打开，刷新详情
        if (detailDialogVisible.value) {
          await loadDetail(currentCompleteApply.value.applyId)
        }
      }
    } catch (error) {
      console.error('结束实习失败:', error)
      ElMessage.error(error.response?.data?.message || '结束实习失败')
    } finally {
      completeLoading.value = false
    }
  } catch (error) {
    // 表单验证失败
  }
}
```

### 6.3 企业管理员页面集成

**文件位置：** `internship-web/src/views/enterprise/StudentManagement.vue`

**添加内容：**

```vue
<!-- 1. 在操作列添加按钮 -->
<el-table-column label="操作" width="360" fixed="right" align="center">
  <template #default="{ row }">
    <el-button link type="primary" size="small" @click="handleView(row)">查看详情</el-button>
    <el-button link type="success" size="small" @click="handleAssignMentor(row)">分配导师</el-button>
    <el-button 
      link 
      type="success" 
      size="small" 
      :icon="Download"
      @click="handleExportStudentReport(row)"
      v-if="row.applyId"
    >
      导出报告
    </el-button>
    <el-button
      v-if="canMarkAsCompleted(row)"
      link
      type="success"
      size="small"
      @click="handleMarkAsCompleted(row)"
    >
      结束实习
    </el-button>
  </template>
</el-table-column>

<!-- 2. 在详情对话框添加按钮 -->
<template #footer>
  <el-button @click="detailDialogVisible = false">关闭</el-button>
  <el-button
    v-if="canMarkAsCompleted(detailData)"
    type="success"
    @click="handleMarkAsCompletedFromDetail"
  >
    结束实习
  </el-button>
</template>

<!-- 3. 添加结束实习对话框（同班主任页面，参考6.2节） -->
```

**添加脚本：**

```javascript
// 导入API（如果还没有）
import { applyApi } from '@/api/internship/apply'

// 添加响应式数据
const completeDialogVisible = ref(false)
const completeFormRef = ref(null)
const currentCompleteApply = ref({})
const completeLoading = ref(false)

const completeForm = reactive({
  endDate: null,
  remark: ''
})

const completeFormRules = {
  endDate: [
    {
      validator: (rule, value, callback) => {
        if (value) {
          const endDate = new Date(value)
          const startDate = currentCompleteApply.value.internshipStartDate
          if (startDate && endDate < new Date(startDate)) {
            callback(new Error('实习结束日期不能早于开始日期'))
          } else {
            callback()
          }
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

// 判断是否可以标记为结束（仅合作企业）
const canMarkAsCompleted = (row) => {
  if (!row) return false
  // 不能标记已删除的
  if (row.deleteFlag === 1) return false
  // 不能标记已解绑的
  if (row.unbindStatus === 2) return false
  // 仅合作企业：status=3（已录用）且未标记结束（status!=7）
  return row.applyType === 1 && row.status === 3 && row.status !== 7
}

// 处理结束实习
const handleMarkAsCompleted = (row) => {
  currentCompleteApply.value = { ...row }
  completeForm.endDate = null
  completeForm.remark = ''
  completeDialogVisible.value = true
}

// 从详情对话框处理结束实习
const handleMarkAsCompletedFromDetail = () => {
  currentCompleteApply.value = { ...detailData.value }
  completeForm.endDate = null
  completeForm.remark = ''
  completeDialogVisible.value = true
}

// 提交结束实习
const handleSubmitComplete = async () => {
  if (!completeFormRef.value) return
  
  try {
    await completeFormRef.value.validate()
    
    completeLoading.value = true
    try {
      const res = await applyApi.completeInternship(
        currentCompleteApply.value.applyId,
        completeForm.endDate,
        completeForm.remark
      )
      if (res.code === 200) {
        ElMessage.success('结束实习成功')
        completeDialogVisible.value = false
        // 刷新列表
        await loadData()
        // 如果详情对话框打开，刷新详情
        if (detailDialogVisible.value) {
          await loadDetail(currentCompleteApply.value.applyId)
        }
      }
    } catch (error) {
      console.error('结束实习失败:', error)
      ElMessage.error(error.response?.data?.message || '结束实习失败')
    } finally {
      completeLoading.value = false
    }
  } catch (error) {
    // 表单验证失败
  }
}
```

### 6.4 企业导师页面集成

**文件位置：** `internship-web/src/views/enterprise/MentorStudentManagement.vue`

**添加内容：**

```vue
<!-- 1. 在操作列添加按钮 -->
<el-table-column label="操作" width="350" fixed="right" align="center">
  <template #default="{ row }">
    <el-button link type="primary" size="small" @click="handleView(row)">查看详情</el-button>
    <el-button 
      link 
      type="warning" 
      size="small" 
      @click="handleAssignAttendanceGroup(row)"
      v-if="row.applyId && row.status === 3"
    >
      分配考勤组
    </el-button>
    <el-button 
      link 
      type="success" 
      size="small" 
      :icon="Download"
      @click="handleExportStudentReport(row)"
      v-if="row.applyId"
    >
      导出报告
    </el-button>
    <el-button
      v-if="canMarkAsCompleted(row)"
      link
      type="success"
      size="small"
      @click="handleMarkAsCompleted(row)"
    >
      结束实习
    </el-button>
  </template>
</el-table-column>

<!-- 2. 在详情对话框添加按钮 -->
<template #footer>
  <el-button @click="detailDialogVisible = false">关闭</el-button>
  <el-button
    v-if="canMarkAsCompleted(detailData)"
    type="success"
    @click="handleMarkAsCompletedFromDetail"
  >
    结束实习
  </el-button>
</template>

<!-- 3. 添加结束实习对话框（同班主任页面，参考6.2节） -->
```

**添加脚本（类似企业管理员页面）：**

```javascript
// 导入API（如果还没有）
import { applyApi } from '@/api/internship/apply'

// 添加响应式数据（同企业管理员页面）
// ... 参考6.3节

// 判断是否可以标记为结束（仅合作企业）
const canMarkAsCompleted = (row) => {
  if (!row) return false
  // 不能标记已删除的
  if (row.deleteFlag === 1) return false
  // 不能标记已解绑的
  if (row.unbindStatus === 2) return false
  // 仅合作企业：status=3（已录用）且未标记结束（status!=7）
  return row.applyType === 1 && row.status === 3 && row.status !== 7
}

// 处理结束实习（同企业管理员页面）
// ... 参考6.3节
```

### 6.5 企业页面：学生评价页面集成

**文件位置：** `internship-web/src/views/enterprise/StudentEvaluation.vue`

**添加内容：**

```vue
<!-- 在操作列添加按钮 -->
<el-table-column label="操作" width="200" fixed="right" align="center">
  <template #default="{ row }">
    <el-button
      v-if="row.evaluationStatus !== 1"
      link
      type="primary"
      size="small"
      @click="handleEvaluate(row)"
    >
      评价
    </el-button>
    <el-button
      v-else
      link
      type="primary"
      size="small"
      @click="handleView(row)"
    >
      查看
    </el-button>
    <el-button
      v-if="canMarkAsCompleted(row)"
      link
      type="success"
      size="small"
      @click="handleMarkAsCompleted(row)"
    >
      结束实习
    </el-button>
  </template>
</el-table-column>

<!-- 添加结束实习对话框（同班主任页面，参考6.2节） -->
```

**添加脚本（类似企业管理员页面）：**

```javascript
// 判断是否可以标记为结束（仅合作企业）
const canMarkAsCompleted = (row) => {
  if (!row) return false
  // 不能标记已删除的
  if (row.deleteFlag === 1) return false
  // 不能标记已解绑的
  if (row.unbindStatus === 2) return false
  // 仅合作企业：status=3（已录用）且未标记结束（status!=7）
  return row.applyType === 1 && row.status === 3 && row.status !== 7
}
```

### 6.6 学生页面显示

**文件位置：** `internship-web/src/views/student/MyInternship.vue`

**修改内容：**

```vue
<!-- 在状态标签区域添加实习结束状态显示 -->
<div style="display: flex; align-items: center; gap: 10px">
  <el-tag :type="getInternshipStatusType(currentInternship.studentInternshipStatus)" size="large">
    {{ getInternshipStatusText(currentInternship.studentInternshipStatus) }}
  </el-tag>
  <!-- 如果实习已结束，显示结束日期 -->
  <el-tag
    v-if="isInternshipCompleted(currentInternship)"
    type="info"
    size="small"
  >
    实习已结束（{{ formatDate(currentInternship.internshipEndDate) }}）
  </el-tag>
  <!-- 其他状态标签... -->
</div>

<!-- 在描述信息中添加结束日期和备注 -->
<el-descriptions-item v-if="isInternshipCompleted(currentInternship)" label="实习结束日期">
  {{ formatDate(currentInternship.internshipEndDate) }}
</el-descriptions-item>
<el-descriptions-item v-if="isInternshipCompleted(currentInternship) && currentInternship.unbindAuditOpinion" label="结束备注" :span="2">
  {{ currentInternship.unbindAuditOpinion }}
</el-descriptions-item>
```

**添加脚本：**

```javascript
// 判断实习是否已结束
const isInternshipCompleted = (internship) => {
  if (!internship) return false
  // 合作企业：status=7
  if (internship.applyType === 1) {
    return internship.status === 7
  }
  // 自主实习：status=13
  if (internship.applyType === 2) {
    return internship.status === 13
  }
  return false
}
```

---

## 七、状态联动详细说明

### 7.1 申请状态（InternshipApply.status）

**更新时机：** 标记实习结束时立即更新

**更新规则：**
- 合作企业：3（已录用）→ 7（实习结束）
- 自主实习：11（实习中）→ 13（实习结束）

**检查点：**
- 标记前检查当前状态是否符合要求
- 标记后检查状态是否已更新

### 7.2 学生实习状态（Student.internshipStatus）

**更新时机：** 标记实习结束时立即更新

**更新规则：**
- 1（实习中）→ 3（已结束）

**检查点：**
- 标记前检查学生状态是否为"实习中"
- 标记后检查学生状态是否已更新为"已结束"

### 7.3 解绑状态（InternshipApply.unbindStatus）

**字段说明：** 解绑状态（在职/离职状态）

**UnbindStatus 枚举值：**
- 0: 未解绑（在职）
- 1: 已申请解绑（待企业管理员审批）
- 2: 已解绑（离职审批通过）
- 3: 已拒绝
- 4: 企业管理员已审批（待学校审批）

**业务逻辑：**
- **解绑**：学生提前离职，需要走解绑审批流程
- **实习结束**：正常完成实习周期，学生离开企业

**⚠️ 重要：如何区分"提前离职"和"实习结束"？**

**方案A：不更新 `unbindStatus`，通过 `status` 区分（推荐）**

**检查规则（标记前）：**
- 如果 `unbindStatus=2`（已解绑），说明已经提前离职，**不能标记为实习结束**
- 如果 `unbindStatus=1`（已申请解绑）或 `4`（企业管理员已审批），说明正在解绑流程中，**可以标记为实习结束**（实习结束会覆盖解绑流程）

**更新规则（标记后）：**
- **不更新 `unbindStatus`**：实习结束后，**保持 `unbindStatus` 原值不变**
- **区分逻辑**：
  - **提前离职**：`status=3`（已录用）+ `unbindStatus=2`（已解绑）→ 前端显示"已离职"
  - **实习结束**：`status=7`（合作企业）或 `status=13`（自主实习）+ `unbindStatus` 保持原值 → 前端显示"实习结束"

**前端判断逻辑：**
```javascript
// 判断是否为实习结束
const isInternshipCompleted = (row) => {
  if (!row) return false
  // 合作企业：status=7
  if (row.applyType === 1) {
    return row.status === 7
  }
  // 自主实习：status=13
  if (row.applyType === 2) {
    return row.status === 13
  }
  return false
}

// 判断是否为提前离职（已解绑）
const isUnbound = (row) => {
  if (!row) return false
  // status=3（已录用）且 unbindStatus=2（已解绑），说明是提前离职
  return row.status === 3 && row.unbindStatus === 2
}

// 获取在职状态显示文本
const getUnbindStatusText = (row) => {
  if (!row) return '在职'
  
  // 优先判断实习结束
  if (isInternshipCompleted(row)) {
    return '实习结束'
  }
  
  // 其次判断提前离职
  if (isUnbound(row)) {
    return '已离职'
  }
  
  // 其他解绑状态
  if (row.unbindStatus === 2) {
    return '已解绑'
  }
  if (row.unbindStatus === 1) {
    return '已申请解绑'
  }
  if (row.unbindStatus === 4) {
    return '企业已审批'
  }
  if (row.unbindStatus === 3) {
    return '解绑已拒绝'
  }
  
  return '在职'
}
```

**实现位置：** 
- 检查：`validateStatusForComplete` 方法
- 更新：`updateApplyForComplete` 方法（不更新 `unbindStatus`）

### 7.4 学生当前申请ID（Student.currentApplyId）

**不更新：** 结束实习后保持原值

**原因：** 保留历史记录，用于查询和统计

### 7.5 学生当前企业ID（Student.currentEnterpriseId）

**不更新：** 结束实习后保持原值

**原因：** 保留历史记录，用于查询和统计

### 7.6 实习结束日期（InternshipApply.internshipEndDate）

**更新时机：** 标记实习结束时

**更新规则：**
- 如果传入了结束日期，使用传入的日期
- 如果没有传入，使用当前日期
- 验证：结束日期不能早于开始日期

### 7.7 备注字段（InternshipApply.unbindAuditOpinion）

**更新时机：** 标记实习结束时（如果传入了备注）

**更新规则：**
- 如果传入了备注，保存到 `unbindAuditOpinion` 字段
- 如果没有传入，不更新该字段（保持原值）

**注意：** 复用现有字段，用于存储结束实习的备注信息

### 7.8 状态联动完整检查清单

**在实现和测试时，必须确保以下所有状态都被正确处理：**

| 序号 | 表名 | 字段名 | 操作 | 验证点 |
|------|------|--------|------|--------|
| 1 | `internship_apply` | `status` | 更新 | 合作企业：3→7，自主实习：11→13 |
| 2 | `internship_apply` | `internship_end_date` | 更新 | 正确设置结束日期 |
| 3 | `internship_apply` | `unbind_audit_opinion` | 可选更新 | 有备注时正确保存 |
| 4 | `internship_apply` | `unbind_status` | 检查+保持不变 | 已解绑时拒绝，结束后不更新（通过status区分） |
| 5 | `student_info` | `internship_status` | 更新 | 1→3 |
| 6 | `student_info` | `current_apply_id` | 保持不变 | 保持原值 |
| 7 | `student_info` | `current_enterprise_id` | 保持不变 | 保持原值 |

---

## 八、权限控制详细说明

### 8.1 合作企业实习权限控制

**企业标记（优先）：**
```java
// 检查当前用户是否是企业管理员或企业导师
Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
if (currentUserEnterpriseId != null && 
    apply.getEnterpriseId() != null &&
    currentUserEnterpriseId.equals(apply.getEnterpriseId())) {
    // 可以标记
    return;
}
```

**班主任标记（容错）：**
```java
// 检查当前用户是否是班主任，且学生属于管理的班级
if (dataPermissionUtil.hasRole(Constants.ROLE_CLASS_TEACHER)) {
    Student student = studentMapper.selectById(apply.getStudentId());
    List<Long> classIds = dataPermissionUtil.getCurrentUserClassIds();
    if (classIds != null && classIds.contains(student.getClassId())) {
        // 可以标记（但需要检查企业是否已标记）
        // 如果企业已标记，班主任不能再标记
        if (isInternshipCompleted(apply)) {
            throw new BusinessException("该实习已被企业标记为结束，无需重复标记");
        }
        return;
    }
}
```

**企业覆盖班主任标记：**
```java
// 如果班主任已标记，企业可以覆盖
if (isInternshipCompleted(apply)) {
    // 检查是否是企业标记的
    // 如果是班主任标记的，企业可以覆盖（需要确认）
    // 这里可以添加覆盖确认逻辑
}
```

### 8.2 自主实习权限控制

**班主任标记：**
```java
// 检查当前用户是否是班主任，且学生属于管理的班级
if (dataPermissionUtil.hasRole(Constants.ROLE_CLASS_TEACHER)) {
    Student student = studentMapper.selectById(apply.getStudentId());
    List<Long> classIds = dataPermissionUtil.getCurrentUserClassIds();
    if (classIds != null && classIds.contains(student.getClassId())) {
        // 可以标记
        return;
    }
}
```

**学校管理员/学院负责人标记：**
```java
// 检查学校管理员或学院负责人权限
if (dataPermissionUtil.hasRole(Constants.ROLE_SCHOOL_ADMIN) ||
    dataPermissionUtil.hasRole(Constants.ROLE_COLLEGE_LEADER)) {
    // 可以标记（需要验证学生是否属于管理的学校/学院）
    return;
}
```

---

## 九、前端页面显示逻辑

### 9.1 按钮显示条件

**班主任页面（InternshipApplyAudit.vue）：**

```javascript
const canMarkAsCompleted = (row) => {
  // 不能标记已删除的
  if (row.deleteFlag === 1) return false
  
  // 不能标记已解绑的
  if (row.unbindStatus === 2) return false
  
  // 合作企业：status=3（已录用）且未标记结束（status!=7）
  if (row.applyType === 1) {
    return row.status === 3 && row.status !== 7
  }
  
  // 自主实习：status=11（实习中）且未标记结束（status!=13）
  if (row.applyType === 2) {
    return row.status === 11 && row.status !== 13
  }
  
  return false
}
```

**企业管理员页面（StudentManagement.vue）：**

```javascript
const canMarkAsCompleted = (row) => {
  if (!row) return false
  
  // 仅合作企业可以标记
  if (row.applyType !== 1) return false
  
  // 不能标记已删除的
  if (row.deleteFlag === 1) return false
  
  // 不能标记已解绑的
  if (row.unbindStatus === 2) return false
  
  // status=3（已录用）且未标记结束（status!=7）
  return row.status === 3 && row.status !== 7
}
```

**企业导师页面（MentorStudentManagement.vue）：**

```javascript
const canMarkAsCompleted = (row) => {
  if (!row) return false
  
  // 仅合作企业可以标记
  if (row.applyType !== 1) return false
  
  // 不能标记已删除的
  if (row.deleteFlag === 1) return false
  
  // 不能标记已解绑的
  if (row.unbindStatus === 2) return false
  
  // status=3（已录用）且未标记结束（status!=7）
  // 注意：企业导师只能标记分配给自己的学生（后端会验证）
  return row.status === 3 && row.status !== 7
}
```

**企业页面：学生评价页面（StudentEvaluation.vue）：**

```javascript
const canMarkAsCompleted = (row) => {
  if (!row) return false
  
  // 仅合作企业可以标记
  if (row.applyType !== 1) return false
  
  // 不能标记已删除的
  if (row.deleteFlag === 1) return false
  
  // 不能标记已解绑的
  if (row.unbindStatus === 2) return false
  
  // status=3（已录用）且未标记结束（status!=7）
  return row.status === 3 && row.status !== 7
}
```

### 9.2 状态显示

**⚠️ 重要：区分"提前离职"和"实习结束"**

**状态判断逻辑：**

```javascript
// 判断是否为实习结束
const isInternshipCompleted = (row) => {
  if (!row) return false
  // 合作企业：status=7
  if (row.applyType === 1) {
    return row.status === 7
  }
  // 自主实习：status=13
  if (row.applyType === 2) {
    return row.status === 13
  }
  return false
}

// 判断是否为提前离职（已解绑）
const isUnbound = (row) => {
  if (!row) return false
  // status=3（已录用）且 unbindStatus=2（已解绑），说明是提前离职
  return row.status === 3 && row.unbindStatus === 2
}

// 获取在职状态显示文本
const getUnbindStatusText = (row) => {
  if (!row) return '在职'
  
  // 优先判断实习结束
  if (isInternshipCompleted(row)) {
    return '实习结束'
  }
  
  // 其次判断提前离职
  if (isUnbound(row)) {
    return '已离职'
  }
  
  // 其他解绑状态
  if (row.unbindStatus === 2) {
    return '已解绑'
  }
  if (row.unbindStatus === 1) {
    return '已申请解绑'
  }
  if (row.unbindStatus === 4) {
    return '企业已审批'
  }
  if (row.unbindStatus === 3) {
    return '解绑已拒绝'
  }
  
  return '在职'
}

// 获取在职状态标签类型
const getUnbindStatusType = (row) => {
  if (!row) return 'success'
  
  // 实习结束：使用 info 类型（蓝色）
  if (isInternshipCompleted(row)) {
    return 'info'
  }
  
  // 提前离职：使用 danger 类型（红色）
  if (isUnbound(row)) {
    return 'danger'
  }
  
  // 其他解绑状态
  if (row.unbindStatus === 2) {
    return 'warning'
  }
  if (row.unbindStatus === 1 || row.unbindStatus === 4) {
    return 'warning'
  }
  
  return 'success'
}
```

**列表状态列显示：**
```vue
<!-- 申请状态列 -->
<el-table-column label="申请状态" width="120" align="center">
  <template #default="{ row }">
    <el-tag :type="getApplyStatusType(row.status, row.applyType)" size="small">
      {{ getApplyStatusText(row.status, row.applyType) }}
    </el-tag>
  </template>
</el-table-column>

<!-- 在职状态列 -->
<el-table-column label="在职状态" width="150" align="center">
  <template #default="{ row }">
    <el-tag :type="getUnbindStatusType(row)" size="small">
      {{ getUnbindStatusText(row) }}
    </el-tag>
  </template>
</el-table-column>
```

**详情页面显示：**
```vue
<el-descriptions-item label="申请状态">
  <el-tag :type="getApplyStatusType(detailData.status, detailData.applyType)" size="small">
    {{ getApplyStatusText(detailData.status, detailData.applyType) }}
  </el-tag>
</el-descriptions-item>

<el-descriptions-item label="在职状态">
  <el-tag :type="getUnbindStatusType(detailData)" size="small">
    {{ getUnbindStatusText(detailData) }}
  </el-tag>
</el-descriptions-item>

<!-- 如果是实习结束，显示结束日期和备注 -->
<el-descriptions-item v-if="isInternshipCompleted(detailData)" label="实习结束日期">
  {{ formatDate(detailData.internshipEndDate) }}
</el-descriptions-item>
<el-descriptions-item v-if="isInternshipCompleted(detailData) && detailData.unbindAuditOpinion" label="结束备注" :span="2">
  {{ detailData.unbindAuditOpinion }}
</el-descriptions-item>

<!-- 如果是提前离职，显示解绑信息 -->
<el-descriptions-item v-if="isUnbound(detailData)" label="解绑原因" :span="2">
  {{ detailData.unbindReason || '-' }}
</el-descriptions-item>
<el-descriptions-item v-if="isUnbound(detailData) && detailData.unbindAuditOpinion" label="解绑备注" :span="2">
  {{ detailData.unbindAuditOpinion }}
</el-descriptions-item>
```

**状态显示规则总结：**

| 情况 | status | unbindStatus | 显示文本 | 标签类型 | 说明 |
|------|--------|--------------|----------|----------|------|
| 实习结束（合作企业） | 7 | 0/1/3/4 | "实习结束" | info（蓝色） | 正常完成实习 |
| 实习结束（自主实习） | 13 | 0/1/3/4 | "实习结束" | info（蓝色） | 正常完成实习 |
| 提前离职（解绑） | 3 | 2 | "已离职" | danger（红色） | 提前离职 |
| 已解绑（其他） | 其他 | 2 | "已解绑" | warning（黄色） | 其他情况 |
| 已申请解绑 | 3 | 1 | "已申请解绑" | warning（黄色） | 解绑流程中 |
| 企业已审批 | 3 | 4 | "企业已审批" | warning（黄色） | 解绑流程中 |
| 在职 | 3 | 0 | "在职" | success（绿色） | 正常在职 |

---

## 十、测试要点

### 10.1 功能测试

1. **单个标记功能**
   - 企业管理员标记合作企业实习（实习学生管理页面）
   - 企业导师标记合作企业实习（我指导的学生页面）
   - 企业管理员/导师在学生评价页面标记
   - 班主任标记合作企业实习（容错）
   - 班主任标记自主实习
   - 重复标记（应该提示已结束）
   - 状态不符合要求（应该提示错误）
   - 权限不足（应该提示无权限）
   - 企业导师只能标记分配给自己的学生

2. **批量标记功能**
   - 批量标记多个申请
   - 部分成功部分失败的处理

3. **权限测试**
   - 不同角色的权限验证
   - 跨权限操作应该被拒绝
   - 企业管理员只能标记本企业的学生
   - 企业导师只能标记分配给自己的学生
   - 企业标记后，班主任不能再标记
   - 企业管理员和企业导师可以标记同一企业的学生（都符合权限）

### 10.2 状态联动测试

**必须测试所有状态字段的联动更新：**

1. **申请状态更新（InternshipApply.status）**
   - 合作企业：3 → 7
   - 自主实习：11 → 13
   - 验证：状态值正确更新

2. **实习结束日期更新（InternshipApply.internshipEndDate）**
   - 传入日期时，使用传入的日期
   - 未传入日期时，使用当前日期
   - 验证：日期正确设置

3. **备注字段更新（InternshipApply.unbindAuditOpinion）**
   - 传入备注时，正确保存
   - 未传入备注时，不更新（保持原值）
   - 验证：备注内容正确保存

4. **学生状态更新（Student.internshipStatus）**
   - 1（实习中）→ 3（已结束）
   - 验证：学生状态正确更新

5. **解绑状态联动（InternshipApply.unbindStatus）**
   - 标记前检查：已解绑的不能标记为结束（已解绑表示提前离职）
   - 标记后更新：**不更新 `unbindStatus`**（保持原值），通过 `status` 字段区分"提前离职"和"实习结束"
   - 区分逻辑：
     - 提前离职：`status=3` + `unbindStatus=2` → 显示"已离职"
     - 实习结束：`status=7` 或 `status=13` + `unbindStatus` 保持原值 → 显示"实习结束"
   - 验证：已解绑时正确拒绝，结束后 `unbindStatus` 保持不变

6. **学生当前申请ID（Student.currentApplyId）**
   - 结束实习后保持不变
   - 验证：值未改变

7. **学生当前企业ID（Student.currentEnterpriseId）**
   - 结束实习后保持不变
   - 验证：值未改变

8. **日期验证**
   - 结束日期不能早于开始日期
   - 验证：日期校验正确

### 10.3 页面显示测试

1. **按钮显示**
   - 班主任页面：符合条件的显示"结束实习"按钮（合作企业和自主实习）
   - 企业管理员页面：符合条件的显示"结束实习"按钮（仅合作企业）
   - 企业导师页面：符合条件的显示"结束实习"按钮（仅合作企业，且是分配给自己的学生）
   - 企业评价页面：符合条件的显示"结束实习"按钮（仅合作企业）
   - 不符合条件的不显示（已结束、已解绑、状态不符合等）

2. **状态显示**
   - 已结束的显示"实习结束"标签
   - 显示结束日期和备注

---

## 十一、实施步骤

### 11.1 第一阶段：后端开发
1. 添加 Service 接口方法
2. 实现 Service 方法（包括权限检查、状态验证、联动更新）
3. 添加 Controller 接口
4. 编写单元测试

### 11.2 第二阶段：前端开发
1. 添加 API 接口封装（completeInternship、batchCompleteInternship）
2. 在班主任页面（InternshipApplyAudit.vue）添加标记功能
3. 在企业管理员页面（StudentManagement.vue）添加标记功能
4. 在企业导师页面（MentorStudentManagement.vue）添加标记功能
5. 在企业评价页面（StudentEvaluation.vue）添加标记功能
6. 在学生页面（MyInternship.vue）添加状态显示
7. 添加结束实习对话框（可复用）

### 11.3 第三阶段：测试和优化
1. 功能测试
2. 权限测试
3. 状态联动测试
4. 页面显示测试
5. 性能优化

---

## 十二、注意事项

1. **状态一致性**
   - 确保申请状态和学生状态同步更新
   - 使用事务保证原子性

2. **权限控制**
   - 严格按照权限设计实现
   - 企业管理员只能标记本企业的学生
   - 企业导师只能标记分配给自己的学生
   - 企业标记后，班主任不能再标记
   - 后端必须验证权限，前端显示只是辅助

3. **数据完整性**
   - 结束日期不能早于开始日期
   - 已解绑的不能标记为结束（已解绑表示提前离职）
   - 实习结束后不更新 `unbindStatus`（保持原值），通过 `status` 字段区分"提前离职"和"实习结束"

4. **向后兼容**
   - 不影响现有的解绑功能
   - 不影响现有的评价功能

5. **用户体验**
   - 结束实习后及时刷新页面数据
   - 提供清晰的提示信息

---

## 十三、扩展功能

1. **自动标记功能**
   - 定时任务：每天检查实习结束日期，自动标记已到期的实习

2. **标记历史记录**
   - 记录标记操作的历史
   - 支持查看标记记录

3. **评价提醒**
   - 结束实习后自动发送评价提醒
   - 支持配置提醒时间

4. **统计报表**
   - 统计已结束的实习数量
   - 统计实习时长分布

