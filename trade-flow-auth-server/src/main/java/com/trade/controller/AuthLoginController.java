package com.trade.controller;

import com.trade.common.response.ApiResponse;
import com.trade.request.auth.AuthLoginRequest;
import com.trade.response.auth.AccessTokenResponse;
import com.trade.service.AuthLoginService;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * 账号密码登录入口。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthLoginController {

    private final AuthLoginService authLoginService;

    public AuthLoginController(AuthLoginService authLoginService) {
        this.authLoginService = Objects.requireNonNull(authLoginService, "authLoginService");
    }

    @PostMapping(value = "/login")
    public ResponseEntity<ApiResponse<AccessTokenResponse>> login(@RequestBody AuthLoginRequest request) {

        AccessTokenResponse token = authLoginService.login(request);
        // 登录响应包含 JWT，要求浏览器和代理不要缓存这份响应，减少令牌泄露风险。
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).header(HttpHeaders.PRAGMA, "no-cache").body(ApiResponse.success(token));
    }
}