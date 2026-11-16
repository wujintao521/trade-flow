package com.trade.response.auth;

import java.util.List;

/**
 * 当前登录用户信息，不返回 JWT、密码或密码哈希。
 */
public record AuthCurrentUserResponse(Long userId, String username, String displayName, List<String> roles) {

    public AuthCurrentUserResponse {
        roles = List.copyOf(roles);
    }
}