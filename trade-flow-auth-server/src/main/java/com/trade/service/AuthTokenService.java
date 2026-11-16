package com.trade.service;

import com.trade.response.auth.AccessTokenResponse;

import org.springframework.security.core.Authentication;

/**
 * 为已通过认证的账号签发访问令牌。
 */
public interface AuthTokenService {

    AccessTokenResponse issueAccessToken(Authentication authentication);

}