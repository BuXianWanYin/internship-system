package com.server.internshipserver.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.domain.user.User;
import com.server.internshipserver.mapper.user.UserMapper;
import com.server.internshipserver.service.user.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 用户管理Service实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @Override
    public User getUserByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return null;
        }
        
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username)
               .eq(User::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        return this.getOne(wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public User addUser(User user) {
        // 参数校验
        if (!StringUtils.hasText(user.getUsername())) {
            throw new BusinessException("用户名不能为空");
        }
        if (!StringUtils.hasText(user.getPassword())) {
            throw new BusinessException("密码不能为空");
        }
        if (!StringUtils.hasText(user.getRealName())) {
            throw new BusinessException("真实姓名不能为空");
        }
        
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername())
               .eq(User::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        User existUser = this.getOne(wrapper);
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }
        
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // 设置默认值
        if (user.getStatus() == null) {
            user.setStatus(1); // 默认启用
        }
        user.setDeleteFlag(DeleteFlag.NORMAL.getCode());
        
        // 保存
        this.save(user);
        return user;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public User updateUser(User user) {
        if (user.getUserId() == null) {
            throw new BusinessException("用户ID不能为空");
        }
        
        // 检查用户是否存在
        User existUser = this.getById(user.getUserId());
        if (existUser == null || existUser.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("用户不存在");
        }
        
        // 如果修改了用户名，检查新用户名是否已存在
        if (StringUtils.hasText(user.getUsername()) 
                && !user.getUsername().equals(existUser.getUsername())) {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getUsername, user.getUsername())
                   .ne(User::getUserId, user.getUserId())
                   .eq(User::getDeleteFlag, DeleteFlag.NORMAL.getCode());
            User usernameExistUser = this.getOne(wrapper);
            if (usernameExistUser != null) {
                throw new BusinessException("用户名已存在");
            }
        }
        
        // 如果修改了密码，需要加密
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            // 不更新密码
            user.setPassword(null);
        }
        
        // 更新
        this.updateById(user);
        return this.getById(user.getUserId());
    }
    
    @Override
    public User getUserById(Long userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        
        User user = this.getById(userId);
        if (user == null || user.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("用户不存在");
        }
        
        return user;
    }
    
    @Override
    public Page<User> getUserPage(Page<User> page, String username, String realName, String phone) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        wrapper.eq(User::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        
        // 条件查询
        if (StringUtils.hasText(username)) {
            wrapper.like(User::getUsername, username);
        }
        if (StringUtils.hasText(realName)) {
            wrapper.like(User::getRealName, realName);
        }
        if (StringUtils.hasText(phone)) {
            wrapper.like(User::getPhone, phone);
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(User::getCreateTime);
        
        return this.page(page, wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        
        User user = this.getById(userId);
        if (user == null || user.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("用户不存在");
        }
        
        // 软删除
        user.setDeleteFlag(DeleteFlag.DELETED.getCode());
        return this.updateById(user);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetPassword(Long userId, String newPassword) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (!StringUtils.hasText(newPassword)) {
            throw new BusinessException("新密码不能为空");
        }
        
        User user = this.getById(userId);
        if (user == null || user.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("用户不存在");
        }
        
        // 加密新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        return this.updateById(user);
    }
}

