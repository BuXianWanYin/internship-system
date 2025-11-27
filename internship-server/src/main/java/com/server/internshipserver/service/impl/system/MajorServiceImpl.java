package com.server.internshipserver.service.impl.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.enums.UserStatus;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.common.utils.EntityValidationUtil;
import com.server.internshipserver.common.utils.DataPermissionUtil;
import com.server.internshipserver.domain.system.College;
import com.server.internshipserver.domain.system.Major;
import com.server.internshipserver.mapper.system.CollegeMapper;
import com.server.internshipserver.mapper.system.MajorMapper;
import com.server.internshipserver.service.system.MajorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 专业管理Service实现类
 */
@Service
public class MajorServiceImpl extends ServiceImpl<MajorMapper, Major> implements MajorService {
    
    @Autowired
    private DataPermissionUtil dataPermissionUtil;
    
    @Autowired
    private CollegeMapper collegeMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Major addMajor(Major major) {
        // 参数校验
        if (!StringUtils.hasText(major.getMajorName())) {
            throw new BusinessException("专业名称不能为空");
        }
        if (!StringUtils.hasText(major.getMajorCode())) {
            throw new BusinessException("专业代码不能为空");
        }
        if (major.getCollegeId() == null) {
            throw new BusinessException("所属学院ID不能为空");
        }
        
        // 检查专业代码在同一学院内是否已存在
        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Major::getMajorCode, major.getMajorCode())
               .eq(Major::getCollegeId, major.getCollegeId())
               .eq(Major::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        Major existMajor = this.getOne(wrapper);
        if (existMajor != null) {
            throw new BusinessException("该学院下专业代码已存在");
        }
        
        // 设置默认值
        if (major.getStatus() == null) {
            major.setStatus(UserStatus.ENABLED.getCode()); // 默认启用
        }
        major.setDeleteFlag(DeleteFlag.NORMAL.getCode());
        
        // 保存
        this.save(major);
        return major;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Major updateMajor(Major major) {
        EntityValidationUtil.validateIdNotNull(major.getMajorId(), "专业ID");
        
        // 检查专业是否存在
        Major existMajor = this.getById(major.getMajorId());
        EntityValidationUtil.validateEntityExists(existMajor, "专业");
        
        // 如果修改了专业代码，检查新代码在同一学院内是否已存在
        if (StringUtils.hasText(major.getMajorCode()) 
                && !major.getMajorCode().equals(existMajor.getMajorCode())) {
            LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Major::getMajorCode, major.getMajorCode())
                   .eq(Major::getCollegeId, existMajor.getCollegeId())
                   .ne(Major::getMajorId, major.getMajorId())
                   .eq(Major::getDeleteFlag, DeleteFlag.NORMAL.getCode());
            Major codeExistMajor = this.getOne(wrapper);
            if (codeExistMajor != null) {
                throw new BusinessException("该学院下专业代码已存在");
            }
        }
        
        // 更新
        this.updateById(major);
        return this.getById(major.getMajorId());
    }
    
    @Override
    public Major getMajorById(Long majorId) {
        EntityValidationUtil.validateIdNotNull(majorId, "专业ID");
        
        Major major = this.getById(majorId);
        EntityValidationUtil.validateEntityExists(major, "专业");
        
        return major;
    }
    
    @Override
    public Page<Major> getMajorPage(Page<Major> page, String majorName, Long collegeId, Long schoolId) {
        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        wrapper.eq(Major::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        
        // 条件查询
        if (StringUtils.hasText(majorName)) {
            wrapper.like(Major::getMajorName, majorName);
        }
        
        // 筛选条件：学校ID（如果指定了schoolId，需要通过学院关联）
        if (schoolId != null) {
            // 先查询该学校下的学院ID列表
            List<College> colleges = collegeMapper.selectList(
                    new LambdaQueryWrapper<College>()
                            .eq(College::getSchoolId, schoolId)
                            .eq(College::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                            .select(College::getCollegeId)
            );
            if (colleges != null && !colleges.isEmpty()) {
                List<Long> collegeIds = colleges.stream()
                        .map(College::getCollegeId)
                        .collect(Collectors.toList());
                wrapper.in(Major::getCollegeId, collegeIds);
            } else {
                // 如果该学校没有学院，返回空结果
                wrapper.eq(Major::getMajorId, -1L);
            }
        }
        
        // 筛选条件：学院ID
        if (collegeId != null) {
            wrapper.eq(Major::getCollegeId, collegeId);
        }
        
        // 数据权限过滤：根据学院ID过滤
        // 系统管理员：不添加过滤条件
        // 学校管理员：添加 school_id = 当前用户学校ID（通过学院关联）
        // 学院负责人：添加 college_id = 当前用户学院ID
        Long currentUserCollegeId = dataPermissionUtil.getCurrentUserCollegeId();
        Long currentUserSchoolId = dataPermissionUtil.getCurrentUserSchoolId();
        
        if (currentUserCollegeId != null) {
            // 学院负责人：只能查看本院的专业
            wrapper.eq(Major::getCollegeId, currentUserCollegeId);
        } else if (currentUserSchoolId != null) {
            // 学校管理员：只能查看本校的专业（通过学院关联）
            // 先查询本校的学院ID列表，然后使用in方法
            List<College> colleges = collegeMapper.selectList(
                    new LambdaQueryWrapper<College>()
                            .eq(College::getSchoolId, currentUserSchoolId)
                            .eq(College::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                            .select(College::getCollegeId)
            );
            if (colleges != null && !colleges.isEmpty()) {
                List<Long> collegeIds = colleges.stream()
                        .map(College::getCollegeId)
                        .collect(Collectors.toList());
                wrapper.in(Major::getCollegeId, collegeIds);
            } else {
                // 如果没有学院，返回空结果
                wrapper.eq(Major::getMajorId, -1L);
            }
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(Major::getCreateTime);
        
        return this.page(page, wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMajor(Long majorId) {
        if (majorId == null) {
            throw new BusinessException("专业ID不能为空");
        }
        
        Major major = this.getById(majorId);
        EntityValidationUtil.validateEntityExists(major, "专业");
        
        // 软删除
        major.setDeleteFlag(DeleteFlag.DELETED.getCode());
        return this.updateById(major);
    }
}

