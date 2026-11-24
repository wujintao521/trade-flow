package com.trade.model.security;

import java.io.Serializable;
import java.util.List;

/**
 * 当前请求的认证身份，displayName 在服务端加载资料后才有值。
 */
public record CurrentUser(Long userId, String username, String displayName, List<String> roles) implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 仅根据 JWT 构建身份时，昵称尚未加载。
     */
    public CurrentUser(Long userId, String username, List<String> roles) {
        this(userId, username, null, roles);
    }

    public CurrentUser {

        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("当前用户 ID 必须为正整数");
        }

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("当前用户名不能为空");
        }

        roles = List.copyOf(roles);
    }
}