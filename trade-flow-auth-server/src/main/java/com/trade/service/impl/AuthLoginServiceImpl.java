package com.trade.service.impl;

import com.trade.common.exception.BusinessException;
import com.trade.enums.auth.AuthErrorCode;
import com.trade.request.auth.AuthLoginRequest;
import com.trade.response.auth.AccessTokenResponse;
import com.trade.service.AuthLoginService;
import com.trade.service.AuthTokenService;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * 校验登录请求，完成账号密码认证并签发访问令牌。
 */
@Service
public class AuthLoginServiceImpl implements AuthLoginService {

    private static final int MAX_USERNAME_LENGTH = 64;

    private static final int MAX_PASSWORD_BYTES = 72;

    private final AuthenticationManager authenticationManager;

    private final AuthTokenService authTokenService;

    public AuthLoginServiceImpl(AuthenticationManager authenticationManager, AuthTokenService authTokenService) {
        this.authenticationManager = Objects.requireNonNull(authenticationManager, "authenticationManager");
        this.authTokenService = Objects.requireNonNull(authTokenService, "authTokenService");
    }

    @Override
    public AccessTokenResponse login(AuthLoginRequest request) {
        validateRequest(request);
        String username = request.username().strip();
        //验证用户身份
        UsernamePasswordAuthenticationToken authenticationRequest = UsernamePasswordAuthenticationToken.unauthenticated(username, request.password());
        Authentication authentication = authenticationManager.authenticate(authenticationRequest);
        //验证成功签发token
        return authTokenService.issueAccessToken(authentication);
    }

    private void validateRequest(AuthLoginRequest request) {

        if (request == null) {
            throw invalidRequest("登录参数不能为空");
        }

        if (request.username() == null || request.username().isBlank()) {
            throw invalidRequest("用户名不能为空");
        }

        if (request.username().strip().length() > MAX_USERNAME_LENGTH) {
            throw invalidRequest("用户名不能超过 64 个字符");
        }

        if (request.password() == null || request.password().isBlank()) {
            throw invalidRequest("密码不能为空");
        }

        if (request.password().getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
            throw invalidRequest("密码按 UTF-8 编码后不能超过 72 字节");
        }
    }

    private BusinessException invalidRequest(String message) {
        return new BusinessException(AuthErrorCode.INVALID_LOGIN_REQUEST.getCode(), message);
    }

}