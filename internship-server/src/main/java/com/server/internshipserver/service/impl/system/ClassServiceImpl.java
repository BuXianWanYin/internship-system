package com.server.internshipserver.service.impl.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.common.utils.DataPermissionUtil;
import com.server.internshipserver.domain.system.Class;
import com.server.internshipserver.mapper.system.ClassMapper;
import com.server.internshipserver.service.system.ClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * 班级管理Service实现类
 */
@Service
public class ClassServiceImpl extends ServiceImpl<ClassMapper, Class> implements ClassService {
    
    @Autowired
    private DataPermissionUtil dataPermissionUtil;
    
    private static final int SHARE_CODE_LENGTH = 8;
    private static final int SHARE_CODE_EXPIRE_DAYS = 30;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Class addClass(Class classInfo) {
        // 参数校验
        if (!StringUtils.hasText(classInfo.getClassName())) {
            throw new BusinessException("班级名称不能为空");
        }
        if (!StringUtils.hasText(classInfo.getClassCode())) {
            throw new BusinessException("班级代码不能为空");
        }
        if (classInfo.getMajorId() == null) {
            throw new BusinessException("所属专业ID不能为空");
        }
        if (classInfo.getEnrollmentYear() == null) {
            throw new BusinessException("入学年份不能为空");
        }
        
        // 检查班级代码在同一专业内是否已存在
        LambdaQueryWrapper<Class> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Class::getClassCode, classInfo.getClassCode())
               .eq(Class::getMajorId, classInfo.getMajorId())
               .eq(Class::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        Class existClass = this.getOne(wrapper);
        if (existClass != null) {
            throw new BusinessException("该专业下班级代码已存在");
        }
        
        // 设置默认值
        if (classInfo.getStatus() == null) {
            classInfo.setStatus(1); // 默认启用
        }
        classInfo.setDeleteFlag(DeleteFlag.NORMAL.getCode());
        if (classInfo.getShareCodeUseCount() == null) {
            classInfo.setShareCodeUseCount(0);
        }
        
        // 保存
        this.save(classInfo);
        return classInfo;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Class updateClass(Class classInfo) {
        if (classInfo.getClassId() == null) {
            throw new BusinessException("班级ID不能为空");
        }
        
        // 检查班级是否存在
        Class existClass = this.getById(classInfo.getClassId());
        if (existClass == null || existClass.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("班级不存在");
        }
        
        // 如果修改了班级代码，检查新代码在同一专业内是否已存在
        if (StringUtils.hasText(classInfo.getClassCode()) 
                && !classInfo.getClassCode().equals(existClass.getClassCode())) {
            LambdaQueryWrapper<Class> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Class::getClassCode, classInfo.getClassCode())
                   .eq(Class::getMajorId, existClass.getMajorId())
                   .ne(Class::getClassId, classInfo.getClassId())
                   .eq(Class::getDeleteFlag, DeleteFlag.NORMAL.getCode());
            Class codeExistClass = this.getOne(wrapper);
            if (codeExistClass != null) {
                throw new BusinessException("该专业下班级代码已存在");
            }
        }
        
        // 更新
        this.updateById(classInfo);
        return this.getById(classInfo.getClassId());
    }
    
    @Override
    public Class getClassById(Long classId) {
        if (classId == null) {
            throw new BusinessException("班级ID不能为空");
        }
        
        Class classInfo = this.getById(classId);
        if (classInfo == null || classInfo.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("班级不存在");
        }
        
        return classInfo;
    }
    
    @Override
    public Page<Class> getClassPage(Page<Class> page, String className, Long majorId) {
        LambdaQueryWrapper<Class> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        wrapper.eq(Class::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        
        // 条件查询
        if (StringUtils.hasText(className)) {
            wrapper.like(Class::getClassName, className);
        }
        
        // 数据权限过滤：根据专业ID或班级ID过滤
        // 系统管理员：不添加过滤条件
        // 学校管理员：添加 school_id = 当前用户学校ID（通过专业和学院关联）
        // 学院负责人：添加 college_id = 当前用户学院ID（通过专业关联）
        // 班主任：添加 class_id = 当前用户班级ID
        Long currentUserClassId = dataPermissionUtil.getCurrentUserClassId();
        Long currentUserCollegeId = dataPermissionUtil.getCurrentUserCollegeId();
        Long currentUserSchoolId = dataPermissionUtil.getCurrentUserSchoolId();
        
        if (currentUserClassId != null) {
            // 班主任：只能查看本班的班级信息
            wrapper.eq(Class::getClassId, currentUserClassId);
        } else if (currentUserCollegeId != null) {
            // 学院负责人：只能查看本院的班级（通过专业关联）
            wrapper.inSql(Class::getMajorId, 
                    "SELECT major_id FROM major_info WHERE college_id = " + currentUserCollegeId + " AND delete_flag = 0");
        } else if (currentUserSchoolId != null) {
            // 学校管理员：只能查看本校的班级（通过专业和学院关联）
            wrapper.inSql(Class::getMajorId, 
                    "SELECT major_id FROM major_info WHERE college_id IN " +
                    "(SELECT college_id FROM college_info WHERE school_id = " + currentUserSchoolId + " AND delete_flag = 0) " +
                    "AND delete_flag = 0");
        } else if (majorId != null) {
            // 系统管理员可以指定专业ID查询
            wrapper.eq(Class::getMajorId, majorId);
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(Class::getCreateTime);
        
        return this.page(page, wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteClass(Long classId) {
        if (classId == null) {
            throw new BusinessException("班级ID不能为空");
        }
        
        Class classInfo = this.getById(classId);
        if (classInfo == null || classInfo.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("班级不存在");
        }
        
        // 软删除
        classInfo.setDeleteFlag(DeleteFlag.DELETED.getCode());
        return this.updateById(classInfo);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String generateShareCode(Long classId) {
        Class classInfo = getClassById(classId);
        
        // 如果已有分享码且未过期，直接返回
        if (StringUtils.hasText(classInfo.getShareCode()) 
                && classInfo.getShareCodeExpireTime() != null
                && classInfo.getShareCodeExpireTime().isAfter(LocalDateTime.now())) {
            return classInfo.getShareCode();
        }
        
        // 生成新的分享码
        String shareCode = generateUniqueShareCode();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = now.plusDays(SHARE_CODE_EXPIRE_DAYS);
        
        classInfo.setShareCode(shareCode);
        classInfo.setShareCodeGenerateTime(now);
        classInfo.setShareCodeExpireTime(expireTime);
        classInfo.setShareCodeUseCount(0);
        
        this.updateById(classInfo);
        return shareCode;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String regenerateShareCode(Long classId) {
        Class classInfo = getClassById(classId);
        
        // 生成新的分享码
        String shareCode = generateUniqueShareCode();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = now.plusDays(SHARE_CODE_EXPIRE_DAYS);
        
        classInfo.setShareCode(shareCode);
        classInfo.setShareCodeGenerateTime(now);
        classInfo.setShareCodeExpireTime(expireTime);
        classInfo.setShareCodeUseCount(0); // 重新生成后重置使用次数
        
        this.updateById(classInfo);
        return shareCode;
    }
    
    @Override
    public Class validateShareCode(String shareCode) {
        if (!StringUtils.hasText(shareCode)) {
            return null;
        }
        
        LambdaQueryWrapper<Class> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Class::getShareCode, shareCode)
               .eq(Class::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        Class classInfo = this.getOne(wrapper);
        
        if (classInfo == null) {
            return null;
        }
        
        // 检查分享码是否过期
        if (classInfo.getShareCodeExpireTime() == null 
                || classInfo.getShareCodeExpireTime().isBefore(LocalDateTime.now())) {
            return null;
        }
        
        return classInfo;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementShareCodeUseCount(String shareCode) {
        Class classInfo = validateShareCode(shareCode);
        if (classInfo == null) {
            throw new BusinessException("分享码无效或已过期");
        }
        
        classInfo.setShareCodeUseCount(classInfo.getShareCodeUseCount() + 1);
        this.updateById(classInfo);
    }
    
    /**
     * 生成唯一的分享码
     */
    private String generateUniqueShareCode() {
        Random random = new Random();
        String shareCode;
        int maxAttempts = 100; // 最多尝试100次
        
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < SHARE_CODE_LENGTH; i++) {
                sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
            }
            shareCode = sb.toString();
            
            // 检查是否已存在
            LambdaQueryWrapper<Class> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Class::getShareCode, shareCode)
                   .eq(Class::getDeleteFlag, DeleteFlag.NORMAL.getCode());
            Class existClass = this.getOne(wrapper);
            
            if (existClass == null) {
                break; // 找到唯一的分享码
            }
            
            maxAttempts--;
        } while (maxAttempts > 0);
        
        if (maxAttempts <= 0) {
            throw new BusinessException("生成分享码失败，请重试");
        }
        
        return shareCode;
    }
}

