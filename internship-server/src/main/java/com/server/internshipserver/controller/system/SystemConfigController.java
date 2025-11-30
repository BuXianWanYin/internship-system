package com.server.internshipserver.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.domain.system.SystemConfig;
import com.server.internshipserver.service.system.SystemConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统配置管理控制器
 */
@Api(tags = "系统配置管理")
@RestController
@RequestMapping("/system/config")
public class SystemConfigController {
    
    @Autowired
    private SystemConfigService systemConfigService;
    
    @ApiOperation("添加系统配置")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @PostMapping
    public Result<SystemConfig> addConfig(@RequestBody SystemConfig config) {
        SystemConfig result = systemConfigService.addConfig(config);
        return Result.success("添加配置成功", result);
    }
    
    @ApiOperation("更新系统配置")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @PutMapping("/{id}")
    public Result<SystemConfig> updateConfig(
            @ApiParam(value = "配置ID", required = true) @PathVariable Long id,
            @RequestBody SystemConfig config) {
        config.setConfigId(id);
        SystemConfig result = systemConfigService.updateConfig(config);
        return Result.success("更新配置成功", result);
    }
    
    @ApiOperation("查询配置详情")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @GetMapping("/{id}")
    public Result<SystemConfig> getConfigById(
            @ApiParam(value = "配置ID", required = true) @PathVariable Long id) {
        SystemConfig config = systemConfigService.getConfigById(id);
        return Result.success(config);
    }
    
    @ApiOperation("根据配置键查询配置")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @GetMapping("/key/{key}")
    public Result<SystemConfig> getConfigByKey(
            @ApiParam(value = "配置键", required = true) @PathVariable String key) {
        SystemConfig config = systemConfigService.getConfigByKey(key);
        return Result.success(config);
    }
    
    @ApiOperation("分页查询配置列表")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @GetMapping("/page")
    public Result<Page<SystemConfig>> getConfigPage(
            @ApiParam(value = "页码", example = "1") @RequestParam(defaultValue = "1") Long current,
            @ApiParam(value = "每页数量", example = "10") @RequestParam(defaultValue = "10") Long size,
            @ApiParam(value = "配置键", required = false) @RequestParam(required = false) String configKey,
            @ApiParam(value = "配置类型", required = false) @RequestParam(required = false) String configType) {
        Page<SystemConfig> page = new Page<>(current, size);
        Page<SystemConfig> result = systemConfigService.getConfigPage(page, configKey, configType);
        return Result.success(result);
    }
    
    @ApiOperation("删除配置")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @DeleteMapping("/{id}")
    public Result<?> deleteConfig(
            @ApiParam(value = "配置ID", required = true) @PathVariable Long id) {
        systemConfigService.deleteConfig(id);
        return Result.success("删除配置成功");
    }
    
    @ApiOperation("获取评价权重配置（公开接口，所有已登录用户可访问）")
    @GetMapping("/evaluation-weights")
    public Result<java.util.Map<String, String>> getEvaluationWeights() {
        java.util.Map<String, String> weights = new java.util.HashMap<>();
        
        // 合作企业实习权重
        SystemConfig enterpriseWeight = systemConfigService.getConfigByKey("enterprise_evaluation_weight");
        weights.put("enterprise", enterpriseWeight != null && enterpriseWeight.getConfigValue() != null 
            ? enterpriseWeight.getConfigValue() : "0.4");
        
        SystemConfig schoolWeight = systemConfigService.getConfigByKey("school_evaluation_weight");
        weights.put("school", schoolWeight != null && schoolWeight.getConfigValue() != null 
            ? schoolWeight.getConfigValue() : "0.4");
        
        SystemConfig selfWeight = systemConfigService.getConfigByKey("student_self_evaluation_weight");
        weights.put("self", selfWeight != null && selfWeight.getConfigValue() != null 
            ? selfWeight.getConfigValue() : "0.2");
        
        // 自主实习权重
        SystemConfig selfInternshipSchoolWeight = systemConfigService.getConfigByKey("self_internship_school_weight");
        weights.put("selfInternshipSchool", selfInternshipSchoolWeight != null && selfInternshipSchoolWeight.getConfigValue() != null 
            ? selfInternshipSchoolWeight.getConfigValue() : "0.8");
        
        SystemConfig selfInternshipSelfWeight = systemConfigService.getConfigByKey("self_internship_self_weight");
        weights.put("selfInternshipSelf", selfInternshipSelfWeight != null && selfInternshipSelfWeight.getConfigValue() != null 
            ? selfInternshipSelfWeight.getConfigValue() : "0.2");
        
        return Result.success(weights);
    }
}

