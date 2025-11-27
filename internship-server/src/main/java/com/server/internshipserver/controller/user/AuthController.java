package com.server.internshipserver.controller.user;

import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.common.utils.TokenUtil;
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

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(
            @ApiParam(value = "登录用户信息", required = true) @RequestBody UserInfo loginUser) {
            Map<String, Object> data = authService.login(loginUser);
            return Result.success("登录成功", data);
    }

    @ApiOperation("用户登出")
    @PostMapping("/logout")
    public Result<?> logout(HttpServletRequest request) {
        String token = TokenUtil.getTokenFromRequest(request);
        boolean success = authService.logout(token);
        return success ? Result.success("登出成功") : Result.error("登出失败");
    }

    @ApiOperation("刷新Token")
    @PostMapping("/refresh")
    public Result<Map<String, Object>> refreshToken(
            @ApiParam(value = "Token信息", required = true) @RequestBody Map<String, String> request) {
            String token = request.get("token");
            Map<String, Object> data = authService.refreshToken(token);
            return Result.success("Token刷新成功", data);
    }
}

