package com.trade.security;

import java.io.Serializable;
import java.util.List;

/**
 * 本次请求已认证的用户信息，显示名称来自服务端资料查询。
 */
public record AuthCurrentUser(Long userId, String username, String displayName, List<String> roles) implements Serializable {

    private static final long serialVersionUID = 1L;

    public AuthCurrentUser {
        roles = List.copyOf(roles);
    }
}