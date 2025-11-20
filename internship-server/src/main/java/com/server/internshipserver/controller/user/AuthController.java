package com.server.internshipserver.controller.user;

import com.server.internshipserver.common.constant.Constants;
import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.common.result.ResultCode;
import com.server.internshipserver.common.utils.JwtUtil;
import com.server.internshipserver.common.utils.RedisUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 认证授权控制器
 */
@Api(tags = "认证授权管理")
@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Autowired
    private RedisUtil redisUtil;

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(
            @ApiParam(value = "用户名", required = true) @RequestParam String username,
            @ApiParam(value = "密码", required = true) @RequestParam String password) {
        
        try {
            // 进行身份认证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            
            // 认证成功，生成Token
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);
            
            // 将Token存储到Redis
            String redisKey = Constants.TOKEN_KEY_PREFIX + username;
            redisUtil.set(redisKey, token, 2, TimeUnit.HOURS);
            
            // 构建返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("username", username);
            // TODO: 后续添加用户详细信息
            
            return Result.success("登录成功", data);
        } catch (Exception e) {
            return Result.error(ResultCode.LOGIN_ERROR);
        }
    }

    @ApiOperation("用户登出")
    @PostMapping("/logout")
    public Result<?> logout() {
        // TODO: 从请求头获取Token，从Redis中删除
        // 当前实现为简化版本，后续完善
        return Result.success("登出成功");
    }

    @ApiOperation("刷新Token")
    @PostMapping("/refresh")
    public Result<Map<String, Object>> refreshToken(@RequestParam String token) {
        try {
            String username = jwtUtil.getUsernameFromToken(token);
            String newToken = jwtUtil.refreshToken(token);
            
            // 更新Redis中的Token
            String redisKey = Constants.TOKEN_KEY_PREFIX + username;
            redisUtil.set(redisKey, newToken, 7, TimeUnit.DAYS);
            
            Map<String, Object> data = new HashMap<>();
            data.put("token", newToken);
            
            return Result.success("Token刷新成功", data);
        } catch (Exception e) {
            return Result.error(ResultCode.TOKEN_INVALID);
        }
    }
}

