package com.server.internshipserver.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.domain.system.Class;
import com.server.internshipserver.service.system.ClassService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 班级管理控制器
 */
@Api(tags = "班级管理")
@RestController
@RequestMapping("/api/system/class")
public class ClassController {
    
    @Autowired
    private ClassService classService;
    
    @ApiOperation("添加班级")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER')")
    @PostMapping
    public Result<Class> addClass(@RequestBody Class classInfo) {
        // 注意：学院负责人或班主任的majorId需要在请求中指定，或通过数据权限过滤自动限制
        Class result = classService.addClass(classInfo);
        return Result.success("添加班级成功", result);
    }
    
    @ApiOperation("更新班级信息")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER')")
    @PutMapping("/{id}")
    public Result<Class> updateClass(
            @ApiParam(value = "班级ID", required = true) @PathVariable Long id,
            @RequestBody Class classInfo) {
        classInfo.setClassId(id);
        Class result = classService.updateClass(classInfo);
        return Result.success("更新班级成功", result);
    }
    
    @ApiOperation("查询班级详情")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER', 'ROLE_STUDENT')")
    @GetMapping("/{id}")
    public Result<Class> getClassById(
            @ApiParam(value = "班级ID", required = true) @PathVariable Long id) {
        Class classInfo = classService.getClassById(id);
        return Result.success(classInfo);
    }
    
    @ApiOperation("分页查询班级列表")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER', 'ROLE_STUDENT')")
    @GetMapping("/page")
    public Result<Page<Class>> getClassPage(
            @ApiParam(value = "页码", example = "1") @RequestParam(defaultValue = "1") Long current,
            @ApiParam(value = "每页数量", example = "10") @RequestParam(defaultValue = "10") Long size,
            @ApiParam(value = "班级名称", required = false) @RequestParam(required = false) String className,
            @ApiParam(value = "专业ID", required = false) @RequestParam(required = false) Long majorId) {
        Page<Class> page = new Page<>(current, size);
        // 数据权限过滤已在Service层实现，根据当前登录用户角色自动过滤
        Page<Class> result = classService.getClassPage(page, className, majorId);
        return Result.success(result);
    }
    
    @ApiOperation("停用班级")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER')")
    @DeleteMapping("/{id}")
    public Result<?> deleteClass(
            @ApiParam(value = "班级ID", required = true) @PathVariable Long id) {
        classService.deleteClass(id);
        return Result.success("停用班级成功");
    }
    
    @ApiOperation("生成班级分享码")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_CLASS_TEACHER')")
    @PostMapping("/{id}/share-code")
    public Result<Map<String, Object>> generateShareCode(
            @ApiParam(value = "班级ID", required = true) @PathVariable Long id) {
        String shareCode = classService.generateShareCode(id);
        Class classInfo = classService.getClassById(id);
        
        Map<String, Object> data = new HashMap<>();
        data.put("shareCode", shareCode);
        data.put("generateTime", classInfo.getShareCodeGenerateTime());
        data.put("expireTime", classInfo.getShareCodeExpireTime());
        data.put("useCount", classInfo.getShareCodeUseCount());
        
        return Result.success("生成分享码成功", data);
    }
    
    @ApiOperation("重新生成班级分享码")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_CLASS_TEACHER')")
    @PostMapping("/{id}/share-code/regenerate")
    public Result<Map<String, Object>> regenerateShareCode(
            @ApiParam(value = "班级ID", required = true) @PathVariable Long id) {
        String shareCode = classService.regenerateShareCode(id);
        Class classInfo = classService.getClassById(id);
        
        Map<String, Object> data = new HashMap<>();
        data.put("shareCode", shareCode);
        data.put("generateTime", classInfo.getShareCodeGenerateTime());
        data.put("expireTime", classInfo.getShareCodeExpireTime());
        data.put("useCount", classInfo.getShareCodeUseCount());
        
        return Result.success("重新生成分享码成功", data);
    }
    
    @ApiOperation("查看班级分享码")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_CLASS_TEACHER')")
    @GetMapping("/{id}/share-code")
    public Result<Map<String, Object>> getShareCode(
            @ApiParam(value = "班级ID", required = true) @PathVariable Long id) {
        Class classInfo = classService.getClassById(id);
        
        Map<String, Object> data = new HashMap<>();
        data.put("shareCode", classInfo.getShareCode());
        data.put("generateTime", classInfo.getShareCodeGenerateTime());
        data.put("expireTime", classInfo.getShareCodeExpireTime());
        data.put("useCount", classInfo.getShareCodeUseCount());
        
        return Result.success(data);
    }
    
    @ApiOperation("验证分享码")
    @PostMapping("/share-code/validate")
    public Result<Class> validateShareCode(
            @ApiParam(value = "分享码", required = true) @RequestParam String shareCode) {
        Class classInfo = classService.validateShareCode(shareCode);
        if (classInfo == null) {
            return Result.error(400, "分享码无效或已过期");
        }
        return Result.success("分享码验证成功", classInfo);
    }
}

