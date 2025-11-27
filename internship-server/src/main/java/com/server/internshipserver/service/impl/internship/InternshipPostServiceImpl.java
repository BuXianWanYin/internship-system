package com.server.internshipserver.service.impl.internship;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.enums.AuditStatus;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.enums.InternshipPostStatus;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.common.utils.AuditUtil;
import com.server.internshipserver.common.utils.DataPermissionUtil;
import com.server.internshipserver.common.utils.EntityDefaultValueUtil;
import com.server.internshipserver.common.utils.EntityValidationUtil;
import com.server.internshipserver.common.utils.QueryWrapperUtil;
import com.server.internshipserver.common.utils.UniquenessValidationUtil;
import com.server.internshipserver.domain.internship.InternshipPost;
import com.server.internshipserver.domain.internship.InternshipApply;
import com.server.internshipserver.domain.internship.dto.InternshipPostQueryDTO;
import com.server.internshipserver.domain.user.Enterprise;
import com.server.internshipserver.mapper.internship.InternshipPostMapper;
import com.server.internshipserver.mapper.internship.InternshipApplyMapper;
import com.server.internshipserver.mapper.user.EnterpriseMapper;
import com.server.internshipserver.mapper.user.UserMapper;
import com.server.internshipserver.service.internship.InternshipPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 实习岗位管理Service实现类
 */
@Service
public class InternshipPostServiceImpl extends ServiceImpl<InternshipPostMapper, InternshipPost> implements InternshipPostService {
    
    @Autowired
    private DataPermissionUtil dataPermissionUtil;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private InternshipApplyMapper internshipApplyMapper;
    
    @Autowired
    private EnterpriseMapper enterpriseMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public InternshipPost addPost(InternshipPost post) {
        // 参数校验
        EntityValidationUtil.validateStringNotBlank(post.getPostName(), "岗位名称");
        EntityValidationUtil.validateStringNotBlank(post.getPostCode(), "岗位编号");
        EntityValidationUtil.validateIdNotNull(post.getEnterpriseId(), "企业ID");
        EntityValidationUtil.validateStringNotBlank(post.getWorkLocation(), "工作地点");
        
        // 检查岗位编号是否已存在（同一企业内唯一）
        UniquenessValidationUtil.validateUniqueInScope(this, InternshipPost::getPostCode, post.getPostCode(),
                InternshipPost::getEnterpriseId, post.getEnterpriseId(), InternshipPost::getDeleteFlag, "岗位编号", "企业");
        
        // 设置默认值
        if (post.getStatus() == null) {
            post.setStatus(InternshipPostStatus.PENDING.getCode()); // 默认待审核
        }
        if (post.getRecruitCount() == null) {
            post.setRecruitCount(1);
        }
        if (post.getAppliedCount() == null) {
            post.setAppliedCount(0);
        }
        if (post.getAcceptedCount() == null) {
            post.setAcceptedCount(0);
        }
        EntityDefaultValueUtil.setDefaultValues(post);
        
        // 保存
        this.save(post);
        return post;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public InternshipPost updatePost(InternshipPost post) {
        if (post.getPostId() == null) {
            throw new BusinessException("岗位ID不能为空");
        }
        
        // 检查岗位是否存在
        InternshipPost existPost = this.getById(post.getPostId());
        EntityValidationUtil.validateEntityExists(existPost, "岗位");
        
        // 数据权限：企业管理员只能修改自己企业的岗位
        Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
        if (currentUserEnterpriseId != null && !currentUserEnterpriseId.equals(existPost.getEnterpriseId())) {
            throw new BusinessException("无权修改该岗位");
        }
        
        // 如果状态为已发布或已关闭，不允许修改
        if (existPost.getStatus() != null && (existPost.getStatus().equals(InternshipPostStatus.PUBLISHED.getCode()) || existPost.getStatus().equals(InternshipPostStatus.CLOSED.getCode()))) {
            throw new BusinessException("已发布或已关闭的岗位不允许修改");
        }
        
        // 如果修改了岗位编号，检查新编号是否已存在
        if (StringUtils.hasText(post.getPostCode()) 
                && !post.getPostCode().equals(existPost.getPostCode())) {
            LambdaQueryWrapper<InternshipPost> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(InternshipPost::getEnterpriseId, existPost.getEnterpriseId())
                   .eq(InternshipPost::getPostCode, post.getPostCode())
                   .ne(InternshipPost::getPostId, post.getPostId())
                   .eq(InternshipPost::getDeleteFlag, DeleteFlag.NORMAL.getCode());
            InternshipPost codeExistPost = this.getOne(wrapper);
            if (codeExistPost != null) {
                throw new BusinessException("该企业下岗位编号已存在");
            }
        }
        
        // 更新
        this.updateById(post);
        return this.getById(post.getPostId());
    }
    
    @Override
    public InternshipPost getPostById(Long postId) {
        EntityValidationUtil.validateIdNotNull(postId, "岗位ID");
        
        InternshipPost post = this.getById(postId);
        EntityValidationUtil.validateEntityExists(post, "岗位");
        
        // 填充关联字段
        fillPostRelatedFields(post);
        
        return post;
    }
    
    @Override
    public Page<InternshipPost> getPostPage(Page<InternshipPost> page, InternshipPostQueryDTO queryDTO) {
        LambdaQueryWrapper<InternshipPost> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        QueryWrapperUtil.notDeleted(wrapper, InternshipPost::getDeleteFlag);
        
        // 数据权限过滤
        Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
        List<Long> cooperationEnterpriseIds = dataPermissionUtil.getCooperationEnterpriseIds();
        
        // 企业管理员：只能查看自己企业的岗位
        if (currentUserEnterpriseId != null) {
            wrapper.eq(InternshipPost::getEnterpriseId, currentUserEnterpriseId);
        }
        // 学生端：只显示合作企业的岗位
        else if (queryDTO != null && queryDTO.getCooperationOnly() != null && queryDTO.getCooperationOnly() && cooperationEnterpriseIds != null) {
            if (cooperationEnterpriseIds.isEmpty()) {
                // 如果没有合作关系，返回空列表
                return page;
            }
            wrapper.in(InternshipPost::getEnterpriseId, cooperationEnterpriseIds);
        }
        // 学校管理员和班主任：只显示合作企业的岗位
        else if (EntityValidationUtil.isNotEmpty(cooperationEnterpriseIds)) {
            wrapper.in(InternshipPost::getEnterpriseId, cooperationEnterpriseIds);
        }
        
        // 条件查询
        if (queryDTO != null) {
            if (StringUtils.hasText(queryDTO.getPostName())) {
                wrapper.like(InternshipPost::getPostName, queryDTO.getPostName());
            }
            if (queryDTO.getEnterpriseId() != null) {
                wrapper.eq(InternshipPost::getEnterpriseId, queryDTO.getEnterpriseId());
            }
            if (queryDTO.getStatus() != null) {
                wrapper.eq(InternshipPost::getStatus, queryDTO.getStatus());
            }
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(InternshipPost::getCreateTime);
        
        Page<InternshipPost> result = this.page(page, wrapper);
        
        // 填充关联字段
        if (EntityValidationUtil.hasRecords(result)) {
            for (InternshipPost post : result.getRecords()) {
                fillPostRelatedFields(post);
            }
        }
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditPost(Long postId, Integer auditStatus, String auditOpinion) {
        if (postId == null) {
            throw new BusinessException("岗位ID不能为空");
        }
        if (auditStatus == null || 
                (!auditStatus.equals(AuditStatus.APPROVED.getCode()) && !auditStatus.equals(AuditStatus.REJECTED.getCode()))) {
            throw new BusinessException("审核状态无效");
        }
        
        InternshipPost post = this.getById(postId);
        EntityValidationUtil.validateEntityExists(post, "岗位");
        
        // 只有待审核状态才能审核
        EntityValidationUtil.validateStatusEquals(post, InternshipPostStatus.PENDING.getCode(), "岗位", "只有待审核状态的岗位才能审核");
        
        // 设置审核信息
        AuditUtil.setAuditInfo(post, auditStatus, auditOpinion, userMapper);
        
        return this.updateById(post);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean publishPost(Long postId) {
        if (postId == null) {
            throw new BusinessException("岗位ID不能为空");
        }
        
        InternshipPost post = this.getById(postId);
        EntityValidationUtil.validateEntityExists(post, "岗位");
        
        // 数据权限：企业管理员只能发布自己企业的岗位
        Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
        if (currentUserEnterpriseId != null && !currentUserEnterpriseId.equals(post.getEnterpriseId())) {
            throw new BusinessException("无权发布该岗位");
        }
        
        // 只有已通过审核的才能发布
        EntityValidationUtil.validateStatusEquals(post, InternshipPostStatus.APPROVED.getCode(), "岗位", "只有已通过审核的岗位才能发布");
        
        // 更新状态为已发布
        post.setStatus(InternshipPostStatus.PUBLISHED.getCode());
        post.setPublishTime(LocalDateTime.now());
        return this.updateById(post);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean closePost(Long postId) {
        if (postId == null) {
            throw new BusinessException("岗位ID不能为空");
        }
        
        InternshipPost post = this.getById(postId);
        EntityValidationUtil.validateEntityExists(post, "岗位");
        
        // 数据权限：企业管理员只能关闭自己企业的岗位
        Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
        if (currentUserEnterpriseId != null && !currentUserEnterpriseId.equals(post.getEnterpriseId())) {
            throw new BusinessException("无权关闭该岗位");
        }
        
        // 只有已发布的才能关闭
        EntityValidationUtil.validateStatusEquals(post, InternshipPostStatus.PUBLISHED.getCode(), "岗位", "只有已发布的岗位才能关闭");
        
        // 更新状态为已关闭
        post.setStatus(InternshipPostStatus.CLOSED.getCode());
        post.setCloseTime(LocalDateTime.now());
        return this.updateById(post);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePost(Long postId) {
        if (postId == null) {
            throw new BusinessException("岗位ID不能为空");
        }
        
        InternshipPost post = this.getById(postId);
        EntityValidationUtil.validateEntityExists(post, "岗位");
        
        // 数据权限：企业管理员只能删除自己企业的岗位
        Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
        if (currentUserEnterpriseId != null && !currentUserEnterpriseId.equals(post.getEnterpriseId())) {
            throw new BusinessException("无权删除该岗位");
        }
        
        // 已发布的岗位不允许删除
        if (post.getStatus() != null && post.getStatus().equals(InternshipPostStatus.PUBLISHED.getCode())) {
            throw new BusinessException("已发布的岗位不允许删除");
        }
        
        // 软删除
        post.setDeleteFlag(DeleteFlag.DELETED.getCode());
        return this.updateById(post);
    }
    
    @Override
    public List<InternshipApply> getPostApplications(Long postId) {
        if (postId == null) {
            throw new BusinessException("岗位ID不能为空");
        }
        
        // 检查岗位是否存在
        InternshipPost post = this.getById(postId);
        EntityValidationUtil.validateEntityExists(post, "岗位");
        
        // 数据权限：企业管理员只能查看自己企业岗位的申请
        Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
        if (currentUserEnterpriseId != null && !currentUserEnterpriseId.equals(post.getEnterpriseId())) {
            throw new BusinessException("无权查看该岗位的申请");
        }
        
        // 查询该岗位的所有申请
        LambdaQueryWrapper<InternshipApply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InternshipApply::getPostId, postId)
               .eq(InternshipApply::getDeleteFlag, DeleteFlag.NORMAL.getCode())
               .orderByDesc(InternshipApply::getCreateTime);
        
        return internshipApplyMapper.selectList(wrapper);
    }
    
    /**
     * 填充岗位关联字段
     */
    private void fillPostRelatedFields(InternshipPost post) {
        if (post == null) {
            return;
        }
        
        // 填充企业名称
        if (post.getEnterpriseId() != null) {
            Enterprise enterprise = enterpriseMapper.selectById(post.getEnterpriseId());
            if (enterprise != null) {
                post.setEnterpriseName(enterprise.getEnterpriseName());
            }
        }
    }
}

