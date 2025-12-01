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
import com.server.internshipserver.common.utils.EntityDefaultValueUtil;
import com.server.internshipserver.common.utils.EntityValidationUtil;
import com.server.internshipserver.common.utils.QueryWrapperUtil;
import com.server.internshipserver.common.utils.UniquenessValidationUtil;
import com.server.internshipserver.common.utils.UserUtil;
import com.server.internshipserver.domain.user.Enterprise;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.domain.user.dto.EnterpriseAddDTO;
import com.server.internshipserver.domain.user.dto.AuditEnterpriseDTO;
import com.server.internshipserver.domain.system.School;
import com.server.internshipserver.mapper.user.EnterpriseMapper;
import com.server.internshipserver.mapper.user.UserMapper;
import com.server.internshipserver.service.user.EnterpriseService;
import com.server.internshipserver.service.user.UserService;
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
        
        LambdaQueryWrapper<Enterprise> wrapper = QueryWrapperUtil.buildNotDeletedWrapper(Enterprise::getDeleteFlag);
        wrapper.eq(Enterprise::getEnterpriseCode, enterpriseCode);
        return this.getOne(wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Enterprise registerEnterprise(Enterprise enterprise, String username, String password) {
        // 参数校验
        EntityValidationUtil.validateStringNotBlank(enterprise.getEnterpriseName(), "企业名称");
        EntityValidationUtil.validateStringNotBlank(enterprise.getEnterpriseCode(), "企业代码");
        EntityValidationUtil.validateStringNotBlank(username, "用户名");
        EntityValidationUtil.validateStringNotBlank(password, "密码");
        
        // 检查企业代码是否已存在
        Enterprise existEnterprise = getEnterpriseByEnterpriseCode(enterprise.getEnterpriseCode());
        if (existEnterprise != null) {
            throw new BusinessException("企业代码已存在，请使用其他企业代码");
        }
        
        // 检查统一社会信用代码是否已存在
        if (StringUtils.hasText(enterprise.getUnifiedSocialCreditCode())) {
            UniquenessValidationUtil.validateUnique(this, Enterprise::getUnifiedSocialCreditCode, 
                    enterprise.getUnifiedSocialCreditCode(), Enterprise::getDeleteFlag, "统一社会信用代码");
        }
        
        // 检查用户名是否已存在
        UserInfo existUser = userService.getUserByUsername(username);
        if (existUser != null) {
            throw new BusinessException("用户名已存在，请使用其他用户名");
        }
        
        // 先保存企业信息（获取enterpriseId）
        // 设置默认值：注册后直接通过，账号立即可用
        enterprise.setAuditStatus(AuditStatus.APPROVED.getCode()); // 已通过
        EntityDefaultValueUtil.setDefaultValues(enterprise, UserStatus.ENABLED.getCode()); // 启用状态
        this.save(enterprise);
        
        // 创建企业管理员用户
        UserInfo adminUser = new UserInfo();
        adminUser.setUsername(username);
        adminUser.setPassword(password); // UserService会自动加密
        adminUser.setRealName(enterprise.getContactPerson() != null ? enterprise.getContactPerson() : "企业管理员");
        adminUser.setPhone(enterprise.getContactPhone());
        adminUser.setEmail(enterprise.getContactEmail());
        adminUser.setStatus(UserStatus.ENABLED.getCode()); // 默认启用
        // 注意：不设置roles，因为addUser会检查权限，企业注册时没有权限分配角色
        adminUser = userService.addUser(adminUser);
        
        // 分配企业管理员角色（不检查权限，因为这是注册流程）
        userService.assignRoleToUser(adminUser.getUserId(), Constants.ROLE_ENTERPRISE_ADMIN);
        
        // 更新企业信息，关联用户ID（这就是创建企业管理员实体的逻辑）
        enterprise.setUserId(adminUser.getUserId());
        this.updateById(enterprise);
        
        return enterprise;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Enterprise addEnterprise(Enterprise enterprise) {
        // 参数校验
        EntityValidationUtil.validateStringNotBlank(enterprise.getEnterpriseName(), "企业名称");
        
        // 检查企业代码是否已存在
        if (StringUtils.hasText(enterprise.getEnterpriseCode())) {
            Enterprise existEnterprise = getEnterpriseByEnterpriseCode(enterprise.getEnterpriseCode());
            if (existEnterprise != null) {
                throw new BusinessException("企业代码已存在");
            }
        }
        
        // 检查统一社会信用代码是否已存在
        if (StringUtils.hasText(enterprise.getUnifiedSocialCreditCode())) {
            UniquenessValidationUtil.validateUnique(this, Enterprise::getUnifiedSocialCreditCode, 
                    enterprise.getUnifiedSocialCreditCode(), Enterprise::getDeleteFlag, "统一社会信用代码");
        }
        
        // 设置默认值
        if (enterprise.getAuditStatus() == null) {
            enterprise.setAuditStatus(AuditStatus.APPROVED.getCode()); // 默认已通过
        }
        EntityDefaultValueUtil.setDefaultValuesWithEnabledStatus(enterprise);
        
        // 保存
        this.save(enterprise);
        return enterprise;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Enterprise addEnterpriseWithAdmin(EnterpriseAddDTO addDTO) {
        Enterprise enterprise = addDTO.getEnterprise();
        // 参数校验
        EntityValidationUtil.validateStringNotBlank(enterprise.getEnterpriseName(), "企业名称");
        EntityValidationUtil.validateStringNotBlank(addDTO.getAdminName(), "企业管理员姓名");
        EntityValidationUtil.validateStringNotBlank(addDTO.getAdminPhone(), "企业管理员手机号");
        EntityValidationUtil.validateStringNotBlank(addDTO.getAdminPassword(), "企业管理员初始密码");
        
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
        if (StringUtils.hasText(addDTO.getAdminPhone())) {
            username = addDTO.getAdminPhone();
        } else if (StringUtils.hasText(addDTO.getAdminEmail())) {
            username = addDTO.getAdminEmail().split("@")[0];
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
        adminUser.setPassword(addDTO.getAdminPassword()); // UserService会自动加密
        adminUser.setRealName(addDTO.getAdminName());
        adminUser.setPhone(addDTO.getAdminPhone());
        adminUser.setEmail(addDTO.getAdminEmail());
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
        EntityDefaultValueUtil.setDefaultValues(enterprise);
        
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
    public boolean auditEnterprise(Long enterpriseId, AuditEnterpriseDTO auditDTO) {
        // 获取当前登录用户ID作为审核人
        UserInfo user = UserUtil.getCurrentUser(userMapper);
        Long auditorId = user.getUserId();
        return auditEnterprise(enterpriseId, auditDTO, auditorId);
    }
    
    /**
     * 审核企业（内部方法，包含审核人ID）
     * @param enterpriseId 企业ID
     * @param auditDTO 审核信息
     * @param auditorId 审核人ID
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    private boolean auditEnterprise(Long enterpriseId, AuditEnterpriseDTO auditDTO, Long auditorId) {
        EntityValidationUtil.validateIdNotNull(enterpriseId, "企业ID");
        if (auditDTO == null || auditDTO.getAuditStatus() == null) {
            throw new BusinessException("审核信息不能为空");
        }
        EntityValidationUtil.validateIdNotNull(auditorId, "审核人ID");
        
        Enterprise enterprise = this.getById(enterpriseId);
        EntityValidationUtil.validateEntityExists(enterprise, "企业");
        
        // 更新审核信息
        AuditUtil.setAuditInfo(enterprise, auditDTO.getAuditStatus().getCode(), auditDTO.getAuditOpinion(), auditorId);
        
        // 如果审核通过，激活企业管理员账号
        if (auditDTO.getAuditStatus() == AuditStatus.APPROVED && enterprise.getUserId() != null) {
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
                    if (cooperationEnterpriseIds != null && !cooperationEnterpriseIds.contains(enterpriseId)) {
                        throw new BusinessException("无权查看该企业信息");
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
        QueryWrapperUtil.notDeleted(wrapper, Enterprise::getDeleteFlag);
        
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
    public boolean enableEnterprise(Long enterpriseId) {
        EntityValidationUtil.validateIdNotNull(enterpriseId, "企业ID");
        
        Enterprise enterprise = this.getById(enterpriseId);
        EntityValidationUtil.validateEntityExists(enterprise, "企业");
        
        // 启用企业
        enterprise.setStatus(UserStatus.ENABLED.getCode());
        return this.updateById(enterprise);
    }
    
    @Override
    public List<School> getCooperationSchoolsByEnterpriseId(Long enterpriseId) {
        return cooperationService.getCooperationSchoolsByEnterpriseId(enterpriseId);
    }
}

