package com.trade.response.auth;

/**
 * 访问令牌响应，expiresIn 的单位为秒。
 */
public record AccessTokenResponse(String accessToken, String tokenType, long expiresIn) {
}