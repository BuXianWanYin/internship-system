package com.server.internshipserver.service.internship;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.server.internshipserver.domain.internship.InternshipPost;
import com.server.internshipserver.domain.internship.InternshipApply;
import com.server.internshipserver.domain.internship.dto.InternshipPostQueryDTO;

import java.util.List;

/**
 * 实习岗位管理Service接口
 */
public interface InternshipPostService extends IService<InternshipPost> {
    
    /**
     * 发布岗位
     * @param post 岗位信息
     * @return 添加的岗位信息
     */
    InternshipPost addPost(InternshipPost post);
    
    /**
     * 更新岗位信息
     * @param post 岗位信息
     * @return 更新后的岗位信息
     */
    InternshipPost updatePost(InternshipPost post);
    
    /**
     * 根据ID查询岗位详情
     * @param postId 岗位ID
     * @return 岗位信息
     */
    InternshipPost getPostById(Long postId);
    
    /**
     * 分页查询岗位列表
     * @param page 分页参数
     * @param queryDTO 查询条件
     * @return 岗位列表
     */
    Page<InternshipPost> getPostPage(Page<InternshipPost> page, InternshipPostQueryDTO queryDTO);
    
    /**
     * 审核岗位
     * @param postId 岗位ID
     * @param auditStatus 审核状态（1-已通过，2-已拒绝）
     * @param auditOpinion 审核意见
     * @return 是否成功
     */
    boolean auditPost(Long postId, Integer auditStatus, String auditOpinion);
    
    /**
     * 发布岗位
     * @param postId 岗位ID
     * @return 是否成功
     */
    boolean publishPost(Long postId);
    
    /**
     * 取消发布岗位
     * @param postId 岗位ID
     * @return 是否成功
     */
    boolean unpublishPost(Long postId);
    
    /**
     * 关闭岗位（下架）
     * @param postId 岗位ID
     * @return 是否成功
     */
    boolean closePost(Long postId);
    
    /**
     * 删除岗位（软删除）
     * @param postId 岗位ID
     * @return 是否成功
     */
    boolean deletePost(Long postId);
    
    /**
     * 查询岗位申请情况
     * @param postId 岗位ID
     * @return 申请列表
     */
    List<InternshipApply> getPostApplications(Long postId);
    
    /**
     * 查询所有岗位列表（用于导出）
     * @param queryDTO 查询条件
     * @return 岗位列表
     */
    List<InternshipPost> getAllPosts(InternshipPostQueryDTO queryDTO);
}

