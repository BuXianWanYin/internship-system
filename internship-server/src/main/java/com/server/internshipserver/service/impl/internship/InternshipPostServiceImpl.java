package com.server.internshipserver.service.impl.internship;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.common.utils.DataPermissionUtil;
import com.server.internshipserver.common.utils.SecurityUtil;
import com.server.internshipserver.domain.internship.InternshipPost;
import com.server.internshipserver.domain.internship.InternshipApply;
import com.server.internshipserver.domain.user.Enterprise;
import com.server.internshipserver.domain.user.UserInfo;
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
        if (!StringUtils.hasText(post.getPostName())) {
            throw new BusinessException("岗位名称不能为空");
        }
        if (!StringUtils.hasText(post.getPostCode())) {
            throw new BusinessException("岗位编号不能为空");
        }
        if (post.getEnterpriseId() == null) {
            throw new BusinessException("企业ID不能为空");
        }
        if (!StringUtils.hasText(post.getWorkLocation())) {
            throw new BusinessException("工作地点不能为空");
        }
        
        // 检查岗位编号是否已存在（同一企业内唯一）
        LambdaQueryWrapper<InternshipPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InternshipPost::getEnterpriseId, post.getEnterpriseId())
               .eq(InternshipPost::getPostCode, post.getPostCode())
               .eq(InternshipPost::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        InternshipPost existPost = this.getOne(wrapper);
        if (existPost != null) {
            throw new BusinessException("该企业下岗位编号已存在");
        }
        
        // 设置默认值
        if (post.getStatus() == null) {
            post.setStatus(0); // 默认待审核
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
        post.setDeleteFlag(DeleteFlag.NORMAL.getCode());
        
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
        if (existPost == null || existPost.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("岗位不存在");
        }
        
        // 数据权限：企业管理员只能修改自己企业的岗位
        Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
        if (currentUserEnterpriseId != null && !currentUserEnterpriseId.equals(existPost.getEnterpriseId())) {
            throw new BusinessException("无权修改该岗位");
        }
        
        // 如果状态为已发布或已关闭，不允许修改
        if (existPost.getStatus() != null && (existPost.getStatus() == 3 || existPost.getStatus() == 4)) {
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
        if (postId == null) {
            throw new BusinessException("岗位ID不能为空");
        }
        
        InternshipPost post = this.getById(postId);
        if (post == null || post.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("岗位不存在");
        }
        
        // 填充关联字段
        fillPostRelatedFields(post);
        
        return post;
    }
    
    @Override
    public Page<InternshipPost> getPostPage(Page<InternshipPost> page, String postName, Long enterpriseId,
                                           Integer status, Boolean cooperationOnly) {
        LambdaQueryWrapper<InternshipPost> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        wrapper.eq(InternshipPost::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        
        // 数据权限过滤
        Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
        List<Long> cooperationEnterpriseIds = dataPermissionUtil.getCooperationEnterpriseIds();
        
        // 企业管理员：只能查看自己企业的岗位
        if (currentUserEnterpriseId != null) {
            wrapper.eq(InternshipPost::getEnterpriseId, currentUserEnterpriseId);
        }
        // 学生端：只显示合作企业的岗位
        else if (cooperationOnly != null && cooperationOnly && cooperationEnterpriseIds != null) {
            if (cooperationEnterpriseIds.isEmpty()) {
                // 如果没有合作关系，返回空列表
                return page;
            }
            wrapper.in(InternshipPost::getEnterpriseId, cooperationEnterpriseIds);
        }
        // 学校管理员和班主任：只显示合作企业的岗位
        else if (cooperationEnterpriseIds != null && !cooperationEnterpriseIds.isEmpty()) {
            wrapper.in(InternshipPost::getEnterpriseId, cooperationEnterpriseIds);
        }
        
        // 条件查询
        if (StringUtils.hasText(postName)) {
            wrapper.like(InternshipPost::getPostName, postName);
        }
        if (enterpriseId != null) {
            wrapper.eq(InternshipPost::getEnterpriseId, enterpriseId);
        }
        if (status != null) {
            wrapper.eq(InternshipPost::getStatus, status);
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(InternshipPost::getCreateTime);
        
        Page<InternshipPost> result = this.page(page, wrapper);
        
        // 填充关联字段
        if (result.getRecords() != null && !result.getRecords().isEmpty()) {
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
        if (auditStatus == null || (auditStatus != 1 && auditStatus != 2)) {
            throw new BusinessException("审核状态无效");
        }
        
        InternshipPost post = this.getById(postId);
        if (post == null || post.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("岗位不存在");
        }
        
        // 只有待审核状态才能审核
        if (post.getStatus() == null || post.getStatus() != 0) {
            throw new BusinessException("只有待审核状态的岗位才能审核");
        }
        
        // 设置审核信息
        post.setStatus(auditStatus);
        post.setAuditTime(LocalDateTime.now());
        post.setAuditOpinion(auditOpinion);
        
        // 设置审核人ID
        String username = SecurityUtil.getCurrentUsername();
        if (username != null) {
            UserInfo user = userMapper.selectOne(
                    new LambdaQueryWrapper<UserInfo>()
                            .eq(UserInfo::getUsername, username)
                            .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (user != null) {
                post.setAuditUserId(user.getUserId());
            }
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
        if (post == null || post.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("岗位不存在");
        }
        
        // 数据权限：企业管理员只能发布自己企业的岗位
        Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
        if (currentUserEnterpriseId != null && !currentUserEnterpriseId.equals(post.getEnterpriseId())) {
            throw new BusinessException("无权发布该岗位");
        }
        
        // 只有已通过审核的才能发布
        if (post.getStatus() == null || post.getStatus() != 1) {
            throw new BusinessException("只有已通过审核的岗位才能发布");
        }
        
        // 更新状态为已发布
        post.setStatus(3);
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
        if (post == null || post.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("岗位不存在");
        }
        
        // 数据权限：企业管理员只能关闭自己企业的岗位
        Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
        if (currentUserEnterpriseId != null && !currentUserEnterpriseId.equals(post.getEnterpriseId())) {
            throw new BusinessException("无权关闭该岗位");
        }
        
        // 只有已发布的才能关闭
        if (post.getStatus() == null || post.getStatus() != 3) {
            throw new BusinessException("只有已发布的岗位才能关闭");
        }
        
        // 更新状态为已关闭
        post.setStatus(4);
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
        if (post == null || post.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("岗位不存在");
        }
        
        // 数据权限：企业管理员只能删除自己企业的岗位
        Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
        if (currentUserEnterpriseId != null && !currentUserEnterpriseId.equals(post.getEnterpriseId())) {
            throw new BusinessException("无权删除该岗位");
        }
        
        // 已发布的岗位不允许删除
        if (post.getStatus() != null && post.getStatus() == 3) {
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
        if (post == null || post.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("岗位不存在");
        }
        
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

