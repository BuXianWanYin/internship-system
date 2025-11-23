package com.server.internshipserver.controller.common;

import com.server.internshipserver.common.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@Api(tags = "文件管理")
@RestController
@RequestMapping("/file")
public class FileController {
    
    @Value("${file.upload.path:uploads}")
    private String uploadPath;
    
    @Value("${file.upload.max-size:10485760}")
    private long maxFileSize; // 默认10MB
    
    // 允许的文件类型
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
        // 文档类型
        "doc", "docx", "pdf", "txt", "rtf",
        // 图片类型
        "jpg", "jpeg", "png", "gif", "bmp",
        // 表格类型
        "xls", "xlsx",
        // 压缩文件
        "zip", "rar", "7z"
    );
    
    @ApiOperation("上传文件（支持单个或多个文件）")
    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER', 'ROLE_INSTRUCTOR', 'ROLE_STUDENT', 'ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR')")
    public Result<List<String>> uploadFiles(
            @ApiParam(value = "文件列表", required = true) @RequestParam("files") MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return Result.error("请选择要上传的文件");
        }
        
        List<String> fileUrls = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                // 验证文件
                String error = validateFile(file);
                if (error != null) {
                    errors.add(file.getOriginalFilename() + ": " + error);
                    continue;
                }
                
                // 生成文件名
                String originalFilename = file.getOriginalFilename();
                String extension = getFileExtension(originalFilename);
                String newFileName = generateFileName(extension);
                
                // 创建上传目录（按日期分类）
                String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                String fullUploadPath = uploadPath + File.separator + dateDir;
                File uploadDir = new File(fullUploadPath);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }
                
                // 保存文件
                Path filePath = Paths.get(fullUploadPath, newFileName);
                Files.write(filePath, file.getBytes());
                
                // 生成访问URL（相对路径）
                String fileUrl = "/" + uploadPath.replace("\\", "/") + "/" + dateDir.replace("\\", "/") + "/" + newFileName;
                fileUrls.add(fileUrl);
                
            } catch (IOException e) {
                errors.add(file.getOriginalFilename() + ": 文件保存失败 - " + e.getMessage());
            }
        }
        
        if (fileUrls.isEmpty()) {
            return Result.error("文件上传失败: " + String.join(", ", errors));
        }
        
        String message = "成功上传 " + fileUrls.size() + " 个文件";
        if (!errors.isEmpty()) {
            message += "，失败 " + errors.size() + " 个: " + String.join(", ", errors);
        }
        
        return Result.success(message, fileUrls);
    }
    
    @ApiOperation("上传单个文件")
    @PostMapping("/upload/single")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER', 'ROLE_INSTRUCTOR', 'ROLE_STUDENT', 'ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR')")
    public Result<String> uploadFile(
            @ApiParam(value = "文件", required = true) @RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }
        
        try {
            // 验证文件
            String error = validateFile(file);
            if (error != null) {
                return Result.error(error);
            }
            
            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String newFileName = generateFileName(extension);
            
            // 创建上传目录（按日期分类）
            String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String fullUploadPath = uploadPath + File.separator + dateDir;
            File uploadDir = new File(fullUploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            
            // 保存文件
            Path filePath = Paths.get(fullUploadPath, newFileName);
            Files.write(filePath, file.getBytes());
            
            // 生成访问URL（相对路径）
            String fileUrl = "/" + uploadPath.replace("\\", "/") + "/" + dateDir.replace("\\", "/") + "/" + newFileName;
            
            return Result.success("文件上传成功", fileUrl);
            
        } catch (IOException e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证文件
     */
    private String validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            return "文件不能为空";
        }
        
        // 检查文件大小
        if (file.getSize() > maxFileSize) {
            return "文件大小不能超过 " + (maxFileSize / 1024 / 1024) + "MB";
        }
        
        // 检查文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return "文件名不能为空";
        }
        
        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            return "不支持的文件类型，支持的类型: " + String.join(", ", ALLOWED_EXTENSIONS);
        }
        
        return null;
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }
    
    /**
     * 生成文件名
     */
    private String generateFileName(String extension) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return uuid + "." + extension;
    }
}

