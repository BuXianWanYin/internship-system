package com.server.internshipserver.controller.user;

import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.common.result.ResultCode;
import com.server.internshipserver.common.utils.JwtUtil;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.service.user.AuthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 认证授权控制器
 */
@Api(tags = "认证授权管理")
@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private JwtUtil jwtUtil;

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(
            @ApiParam(value = "登录用户信息", required = true) @RequestBody UserInfo loginUser) {
        try {
            Map<String, Object> data = authService.login(loginUser);
            return Result.success("登录成功", data);
        } catch (Exception e) {
            return Result.error(ResultCode.LOGIN_ERROR.getCode(), e.getMessage());
        }
    }

    @ApiOperation("用户登出")
    @PostMapping("/logout")
    public Result<?> logout(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        boolean success = authService.logout(token);
        return success ? Result.success("登出成功") : Result.error("登出失败");
    }
    
    /**
     * 从请求头获取Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @ApiOperation("刷新Token")
    @PostMapping("/refresh")
    public Result<Map<String, Object>> refreshToken(
            @ApiParam(value = "Token信息", required = true) @RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            Map<String, Object> data = authService.refreshToken(token);
            return Result.success("Token刷新成功", data);
        } catch (Exception e) {
            return Result.error(ResultCode.TOKEN_INVALID.getCode(), e.getMessage());
        }
    }
}

