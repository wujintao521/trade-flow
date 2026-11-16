package com.trade.model.auth;

/**
 * 当前用户资料查询结果，不包含密码哈希。
 */
public record AuthUserProfile(Long userId, String username, String displayName, Integer status) {
}