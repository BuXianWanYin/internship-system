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
 * 实现实习岗位的发布、审核、查询等业务功能
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
        EntityValidationUtil.validateStringNotBlank(post.getWorkLocation(), "工作地点");
        
        if (post.getEnterpriseId() == null) {
            Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
            if (currentUserEnterpriseId == null) {
                throw new BusinessException("企业ID不能为空，请确认您已关联企业");
            }
            post.setEnterpriseId(currentUserEnterpriseId);
        }
        
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
        // 学生端：只显示合作企业的已发布岗位，且招聘人数未满
        else if (queryDTO != null && queryDTO.getCooperationOnly() != null && queryDTO.getCooperationOnly() && cooperationEnterpriseIds != null) {
            if (cooperationEnterpriseIds.isEmpty()) {
                // 如果没有合作关系，返回空列表
                return page;
            }
            wrapper.in(InternshipPost::getEnterpriseId, cooperationEnterpriseIds);
            // 学生只能看到已发布的岗位
            wrapper.eq(InternshipPost::getStatus, InternshipPostStatus.PUBLISHED.getCode());
            // 过滤掉招聘人数已满的岗位（acceptedCount >= recruitCount）
            // 逻辑：如果recruitCount为null，则认为未满；如果acceptedCount为null，则认为0
            // SQL: (recruit_count IS NULL) OR (IFNULL(accepted_count, 0) < recruit_count)
            wrapper.and(w -> w.isNull(InternshipPost::getRecruitCount)
                    .or()
                    .apply("IFNULL(accepted_count, 0) < recruit_count"));
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
        
        // 设置审核信息（审核人、审核时间、审核意见）
        AuditUtil.setAuditInfo(post, auditStatus, auditOpinion, userMapper);
        
        // 根据审核结果设置岗位状态
        if (auditStatus.equals(AuditStatus.APPROVED.getCode())) {
            // 审核通过：设置为已通过状态，等待企业管理员发布
            post.setStatus(InternshipPostStatus.APPROVED.getCode());
        } else if (auditStatus.equals(AuditStatus.REJECTED.getCode())) {
            // 审核拒绝：设置为已拒绝状态
            post.setStatus(InternshipPostStatus.REJECTED.getCode());
        }
        
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
    public boolean unpublishPost(Long postId) {
        if (postId == null) {
            throw new BusinessException("岗位ID不能为空");
        }
        
        InternshipPost post = this.getById(postId);
        EntityValidationUtil.validateEntityExists(post, "岗位");
        
        // 数据权限：企业管理员只能取消发布自己企业的岗位
        Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
        if (currentUserEnterpriseId != null && !currentUserEnterpriseId.equals(post.getEnterpriseId())) {
            throw new BusinessException("无权取消发布该岗位");
        }
        
        // 只有已发布的才能取消发布
        EntityValidationUtil.validateStatusEquals(post, InternshipPostStatus.PUBLISHED.getCode(), 
            "岗位", "只有已发布的岗位才能取消发布");
        
        // 更新状态为已通过（审核通过但未发布）
        post.setStatus(InternshipPostStatus.APPROVED.getCode());
        // 清空发布时间
        post.setPublishTime(null);
        
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
        
        // 数据权限：企业管理员只能下架自己企业的岗位
        Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
        if (currentUserEnterpriseId != null && !currentUserEnterpriseId.equals(post.getEnterpriseId())) {
            throw new BusinessException("无权下架该岗位");
        }
        
        // 只有已发布的才能下架（无论报名人数和录用人数是否已满）
        EntityValidationUtil.validateStatusEquals(post, InternshipPostStatus.PUBLISHED.getCode(), 
            "岗位", "只有已发布的岗位才能下架");
        
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
    
    @Override
    public List<InternshipPost> getAllPosts(InternshipPostQueryDTO queryDTO) {
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
                return new java.util.ArrayList<>();
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
        
        List<InternshipPost> posts = this.list(wrapper);
        
        // 填充关联字段
        if (posts != null && !posts.isEmpty()) {
            for (InternshipPost post : posts) {
                fillPostRelatedFields(post);
            }
        }
        
        return posts;
    }
}

