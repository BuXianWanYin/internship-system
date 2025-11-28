package com.server.internshipserver.common.utils;

import com.server.internshipserver.common.constant.Constants;

import javax.servlet.http.HttpServletRequest;

/**
 * Token工具类
 * 提供从HTTP请求中提取JWT Token的工具方法
 */
public class TokenUtil {
    
    private TokenUtil() {
        // 工具类，禁止实例化
    }
    
    /**
     * 从请求头获取Token
     * @param request HTTP请求
     * @return Token字符串，如果不存在则返回null
     */
    public static String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(Constants.TOKEN_HEADER);
        if (bearerToken != null && bearerToken.startsWith(Constants.TOKEN_PREFIX)) {
            return bearerToken.substring(Constants.TOKEN_PREFIX.length());
        }
        return null;
    }
}

