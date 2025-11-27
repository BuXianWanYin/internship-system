package com.server.internshipserver.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.constant.Constants;
import com.server.internshipserver.common.enums.AuditStatus;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.enums.UserStatus;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.common.utils.AuditUtil;
import com.server.internshipserver.common.utils.DataPermissionUtil;
import com.server.internshipserver.common.utils.EntityValidationUtil;
import com.server.internshipserver.common.utils.UserUtil;
import com.server.internshipserver.domain.user.Enterprise;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.domain.system.School;
import com.server.internshipserver.mapper.user.EnterpriseMapper;
import com.server.internshipserver.mapper.user.UserMapper;
import com.server.internshipserver.service.user.EnterpriseService;
import com.server.internshipserver.service.user.UserService;
import com.server.internshipserver.service.user.EnterpriseRegisterSchoolService;
import com.server.internshipserver.service.cooperation.EnterpriseSchoolCooperationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 企业管理Service实现类
 */
@Service
public class EnterpriseServiceImpl extends ServiceImpl<EnterpriseMapper, Enterprise> implements EnterpriseService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private DataPermissionUtil dataPermissionUtil;
    
    @Autowired
    private EnterpriseSchoolCooperationService cooperationService;
    
    @Autowired
    private EnterpriseRegisterSchoolService enterpriseRegisterSchoolService;
    
    @Override
    public Enterprise getEnterpriseByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        
        LambdaQueryWrapper<Enterprise> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Enterprise::getUserId, userId)
               .eq(Enterprise::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        return this.getOne(wrapper);
    }
    
    @Override
    public Enterprise getEnterpriseByEnterpriseCode(String enterpriseCode) {
        if (!StringUtils.hasText(enterpriseCode)) {
            return null;
        }
        
        LambdaQueryWrapper<Enterprise> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Enterprise::getEnterpriseCode, enterpriseCode)
               .eq(Enterprise::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        return this.getOne(wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Enterprise registerEnterprise(Enterprise enterprise, List<Long> schoolIds) {
        // 参数校验
        if (!StringUtils.hasText(enterprise.getEnterpriseName())) {
            throw new BusinessException("企业名称不能为空");
        }
        
        if (schoolIds == null || schoolIds.isEmpty()) {
            throw new BusinessException("至少选择一个意向合作院校");
        }
        
        // 检查统一社会信用代码是否已存在
        if (StringUtils.hasText(enterprise.getUnifiedSocialCreditCode())) {
            LambdaQueryWrapper<Enterprise> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Enterprise::getUnifiedSocialCreditCode, enterprise.getUnifiedSocialCreditCode())
                   .eq(Enterprise::getDeleteFlag, DeleteFlag.NORMAL.getCode());
            Enterprise existEnterprise = this.getOne(wrapper);
            if (existEnterprise != null) {
                throw new BusinessException("统一社会信用代码已存在");
            }
        }
        
        // 设置默认值
        enterprise.setAuditStatus(AuditStatus.PENDING.getCode()); // 待审核
        if (enterprise.getStatus() == null) {
            enterprise.setStatus(UserStatus.DISABLED.getCode()); // 注册时默认禁用，审核通过后激活
        }
        enterprise.setDeleteFlag(DeleteFlag.NORMAL.getCode());
        
        // 保存企业信息
        this.save(enterprise);
        
        // 保存企业与院校的关联关系
        enterpriseRegisterSchoolService.saveEnterpriseRegisterSchools(enterprise.getEnterpriseId(), schoolIds);
        
        return enterprise;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Enterprise addEnterprise(Enterprise enterprise) {
        // 参数校验
        if (!StringUtils.hasText(enterprise.getEnterpriseName())) {
            throw new BusinessException("企业名称不能为空");
        }
        
        // 检查企业代码是否已存在
        if (StringUtils.hasText(enterprise.getEnterpriseCode())) {
            Enterprise existEnterprise = getEnterpriseByEnterpriseCode(enterprise.getEnterpriseCode());
            if (existEnterprise != null) {
                throw new BusinessException("企业代码已存在");
            }
        }
        
        // 检查统一社会信用代码是否已存在
        if (StringUtils.hasText(enterprise.getUnifiedSocialCreditCode())) {
            LambdaQueryWrapper<Enterprise> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Enterprise::getUnifiedSocialCreditCode, enterprise.getUnifiedSocialCreditCode())
                   .eq(Enterprise::getDeleteFlag, DeleteFlag.NORMAL.getCode());
            Enterprise existEnterprise = this.getOne(wrapper);
            if (existEnterprise != null) {
                throw new BusinessException("统一社会信用代码已存在");
            }
        }
        
        // 设置默认值
        if (enterprise.getAuditStatus() == null) {
            enterprise.setAuditStatus(AuditStatus.APPROVED.getCode()); // 默认已通过
        }
        if (enterprise.getStatus() == null) {
            enterprise.setStatus(UserStatus.ENABLED.getCode()); // 默认启用
        }
        enterprise.setDeleteFlag(DeleteFlag.NORMAL.getCode());
        
        // 保存
        this.save(enterprise);
        return enterprise;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Enterprise addEnterpriseWithAdmin(Enterprise enterprise, String adminName, String adminPhone, String adminEmail, String adminPassword) {
        // 参数校验
        if (!StringUtils.hasText(enterprise.getEnterpriseName())) {
            throw new BusinessException("企业名称不能为空");
        }
        if (!StringUtils.hasText(adminName)) {
            throw new BusinessException("企业管理员姓名不能为空");
        }
        if (!StringUtils.hasText(adminPhone)) {
            throw new BusinessException("企业管理员手机号不能为空");
        }
        if (!StringUtils.hasText(adminPassword)) {
            throw new BusinessException("企业管理员初始密码不能为空");
        }
        
        // 检查企业代码是否已存在
        if (StringUtils.hasText(enterprise.getEnterpriseCode())) {
            Enterprise existEnterprise = getEnterpriseByEnterpriseCode(enterprise.getEnterpriseCode());
            if (existEnterprise != null) {
                throw new BusinessException("企业代码已存在");
            }
        }
        
        // 检查统一社会信用代码是否已存在
        if (StringUtils.hasText(enterprise.getUnifiedSocialCreditCode())) {
            LambdaQueryWrapper<Enterprise> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Enterprise::getUnifiedSocialCreditCode, enterprise.getUnifiedSocialCreditCode())
                   .eq(Enterprise::getDeleteFlag, DeleteFlag.NORMAL.getCode());
            Enterprise existEnterprise = this.getOne(wrapper);
            if (existEnterprise != null) {
                throw new BusinessException("统一社会信用代码已存在");
            }
        }
        
        // 生成用户名（使用手机号或邮箱）
        String username;
        if (StringUtils.hasText(adminPhone)) {
            username = adminPhone;
        } else if (StringUtils.hasText(adminEmail)) {
            username = adminEmail.split("@")[0];
        } else {
            username = "ENT_ADMIN_" + System.currentTimeMillis();
        }
        
        // 检查用户名是否已存在
        UserInfo existUser = userService.getUserByUsername(username);
        if (existUser != null) {
            // 如果用户名已存在，添加时间戳
            username = username + "_" + System.currentTimeMillis();
        }
        
        // 创建企业管理员用户
        UserInfo adminUser = new UserInfo();
        adminUser.setUsername(username);
        adminUser.setPassword(adminPassword); // UserService会自动加密
        adminUser.setRealName(adminName);
        adminUser.setPhone(adminPhone);
        adminUser.setEmail(adminEmail);
        adminUser.setStatus(UserStatus.ENABLED.getCode()); // 默认启用
        adminUser = userService.addUser(adminUser);
        
        // 分配企业管理员角色
        userService.assignRoleToUser(adminUser.getUserId(), Constants.ROLE_ENTERPRISE_ADMIN);
        
        // 设置企业信息
        enterprise.setUserId(adminUser.getUserId());
        if (enterprise.getAuditStatus() == null) {
            enterprise.setAuditStatus(AuditStatus.APPROVED.getCode()); // 默认已通过
        }
        if (enterprise.getStatus() == null) {
            enterprise.setStatus(UserStatus.ENABLED.getCode()); // 默认启用
        }
        enterprise.setDeleteFlag(DeleteFlag.NORMAL.getCode());
        
        // 保存企业
        this.save(enterprise);
        return enterprise;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Enterprise updateEnterprise(Enterprise enterprise) {
        if (enterprise.getEnterpriseId() == null) {
            throw new BusinessException("企业ID不能为空");
        }
        
        // 检查企业是否存在
        Enterprise existEnterprise = this.getById(enterprise.getEnterpriseId());
        EntityValidationUtil.validateEntityExists(existEnterprise, "企业");
        
        // 数据权限检查：只有系统管理员或企业管理员可以编辑企业信息
        // 企业管理员只能编辑自己的企业
        if (!dataPermissionUtil.isSystemAdmin()) {
            UserInfo currentUser = UserUtil.getCurrentUserOrNull(userMapper);
            if (currentUser != null) {
                List<String> roleCodes = userMapper.selectRoleCodesByUserId(currentUser.getUserId());
                // 企业管理员：只能编辑本企业
                if (DataPermissionUtil.hasRole(roleCodes, Constants.ROLE_ENTERPRISE_ADMIN)) {
                    if (!existEnterprise.getUserId().equals(currentUser.getUserId())) {
                        throw new BusinessException("无权编辑该企业信息");
                    }
                } else {
                    // 其他角色（包括学校管理员）不能编辑企业信息
                    throw new BusinessException("无权编辑企业信息");
                }
            }
        }
        
        // 如果修改了企业代码，检查新代码是否已存在
        if (StringUtils.hasText(enterprise.getEnterpriseCode()) 
                && !enterprise.getEnterpriseCode().equals(existEnterprise.getEnterpriseCode())) {
            Enterprise codeExist = getEnterpriseByEnterpriseCode(enterprise.getEnterpriseCode());
            if (codeExist != null) {
                throw new BusinessException("企业代码已存在");
            }
        }
        
        // 如果修改了统一社会信用代码，检查新代码是否已存在
        if (StringUtils.hasText(enterprise.getUnifiedSocialCreditCode()) 
                && !enterprise.getUnifiedSocialCreditCode().equals(existEnterprise.getUnifiedSocialCreditCode())) {
            LambdaQueryWrapper<Enterprise> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Enterprise::getUnifiedSocialCreditCode, enterprise.getUnifiedSocialCreditCode())
                   .ne(Enterprise::getEnterpriseId, enterprise.getEnterpriseId())
                   .eq(Enterprise::getDeleteFlag, DeleteFlag.NORMAL.getCode());
            Enterprise codeExist = this.getOne(wrapper);
            if (codeExist != null) {
                throw new BusinessException("统一社会信用代码已存在");
            }
        }
        
        // 更新
        this.updateById(enterprise);
        return this.getById(enterprise.getEnterpriseId());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditEnterprise(Long enterpriseId, Integer auditStatus, String auditOpinion) {
        // 获取当前登录用户ID作为审核人
        UserInfo user = UserUtil.getCurrentUser(userMapper);
        Long auditorId = user.getUserId();
        return auditEnterprise(enterpriseId, auditStatus, auditOpinion, auditorId);
    }
    
    /**
     * 审核企业（内部方法，包含审核人ID）
     * @param enterpriseId 企业ID
     * @param auditStatus 审核状态：1-通过，2-拒绝（使用AuditStatus枚举）
     * @param auditOpinion 审核意见
     * @param auditorId 审核人ID
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean auditEnterprise(Long enterpriseId, Integer auditStatus, String auditOpinion, Long auditorId) {
        EntityValidationUtil.validateIdNotNull(enterpriseId, "企业ID");
        if (auditStatus == null || (!auditStatus.equals(AuditStatus.APPROVED.getCode()) && !auditStatus.equals(AuditStatus.REJECTED.getCode()))) {
            throw new BusinessException("审核状态无效");
        }
        EntityValidationUtil.validateIdNotNull(auditorId, "审核人ID");
        
        Enterprise enterprise = this.getById(enterpriseId);
        EntityValidationUtil.validateEntityExists(enterprise, "企业");
        
        // 更新审核信息
        AuditUtil.setAuditInfo(enterprise, auditStatus, auditOpinion, auditorId);
        
        // 如果审核通过，激活企业管理员账号
        if (auditStatus.equals(AuditStatus.APPROVED.getCode()) && enterprise.getUserId() != null) {
            UserInfo user = userMapper.selectById(enterprise.getUserId());
            if (user != null && user.getDeleteFlag().equals(DeleteFlag.NORMAL.getCode())) {
                // 激活账号：设置状态为启用
                user.setStatus(UserStatus.ENABLED.getCode());
                userMapper.updateById(user);
            }
        }
        
        return this.updateById(enterprise);
    }
    
    @Override
    public Enterprise getEnterpriseById(Long enterpriseId) {
        if (enterpriseId == null) {
            throw new BusinessException("企业ID不能为空");
        }
        
        Enterprise enterprise = this.getById(enterpriseId);
        EntityValidationUtil.validateEntityExists(enterprise, "企业");
        
        // 数据权限检查：非系统管理员需要检查是否有权限查看该企业
        if (!dataPermissionUtil.isSystemAdmin()) {
            UserInfo currentUser = UserUtil.getCurrentUserOrNull(userMapper);
            if (currentUser != null) {
                List<String> roleCodes = userMapper.selectRoleCodesByUserId(currentUser.getUserId());
                // 企业管理员：只能查看本企业
                if (DataPermissionUtil.hasRole(roleCodes, Constants.ROLE_ENTERPRISE_ADMIN)) {
                    if (!enterprise.getUserId().equals(currentUser.getUserId())) {
                        throw new BusinessException("无权查看该企业信息");
                    }
                } else {
                    // 学校管理员或班主任：只能查看有合作关系的企业
                    List<Long> cooperationEnterpriseIds = dataPermissionUtil.getCooperationEnterpriseIds();
                    if (cooperationEnterpriseIds != null) {
                        if (!cooperationEnterpriseIds.contains(enterpriseId)) {
                            throw new BusinessException("无权查看该企业信息");
                        }
                    }
                }
            }
        }
        
        return enterprise;
    }
    
    @Override
    public Page<Enterprise> getEnterprisePage(Page<Enterprise> page, String enterpriseName, 
                                               String enterpriseCode, Integer auditStatus) {
        LambdaQueryWrapper<Enterprise> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        wrapper.eq(Enterprise::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        
        // 条件查询
        if (StringUtils.hasText(enterpriseName)) {
            wrapper.like(Enterprise::getEnterpriseName, enterpriseName);
        }
        if (StringUtils.hasText(enterpriseCode)) {
            wrapper.eq(Enterprise::getEnterpriseCode, enterpriseCode);
        }
        if (auditStatus != null) {
            wrapper.eq(Enterprise::getAuditStatus, auditStatus);
        }
        
        // 数据权限过滤：根据用户角色自动添加查询条件
        // 系统管理员：不添加过滤条件（可以查看所有企业）
        // 学校管理员：只能查看和本校有合作关系的企业
        // 班主任：只能查看和管理的班级有合作关系的企业
        // 企业管理员：只能查看本企业（通过userId关联）
        if (!dataPermissionUtil.isSystemAdmin()) {
            UserInfo currentUser = UserUtil.getCurrentUserOrNull(userMapper);
            if (currentUser != null) {
                List<String> roleCodes = userMapper.selectRoleCodesByUserId(currentUser.getUserId());
                // 企业管理员：只能查看本企业
                if (DataPermissionUtil.hasRole(roleCodes, Constants.ROLE_ENTERPRISE_ADMIN)) {
                    wrapper.eq(Enterprise::getUserId, currentUser.getUserId());
                } else {
                    // 学校管理员或班主任：只能查看有合作关系的企业
                    List<Long> cooperationEnterpriseIds = dataPermissionUtil.getCooperationEnterpriseIds();
                    if (cooperationEnterpriseIds != null) {
                        if (cooperationEnterpriseIds.isEmpty()) {
                            // 如果没有合作关系，返回空结果
                            wrapper.eq(Enterprise::getEnterpriseId, -1L);
                        } else {
                            wrapper.in(Enterprise::getEnterpriseId, cooperationEnterpriseIds);
                        }
                    }
                }
            }
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(Enterprise::getCreateTime);
        
        return this.page(page, wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteEnterprise(Long enterpriseId) {
        if (enterpriseId == null) {
            throw new BusinessException("企业ID不能为空");
        }
        
        Enterprise enterprise = this.getById(enterpriseId);
        EntityValidationUtil.validateEntityExists(enterprise, "企业");
        
        // 软删除
        enterprise.setDeleteFlag(DeleteFlag.DELETED.getCode());
        return this.updateById(enterprise);
    }
    
    @Override
    public List<School> getCooperationSchoolsByEnterpriseId(Long enterpriseId) {
        return cooperationService.getCooperationSchoolsByEnterpriseId(enterpriseId);
    }
}

