package com.trade.controller;

import com.trade.common.response.ApiResponse;
import com.trade.response.auth.AuthCurrentUserResponse;
import com.trade.service.AuthCurrentUserService;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * 查询当前登录用户的基本信息。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthCurrentUserController {

    private final AuthCurrentUserService authCurrentUserService;

    public AuthCurrentUserController(AuthCurrentUserService authCurrentUserService) {
        this.authCurrentUserService = Objects.requireNonNull(authCurrentUserService, "authCurrentUserService");
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthCurrentUserResponse>> getCurrentUser() {
        AuthCurrentUserResponse user = authCurrentUserService.getCurrentUser();
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).header(HttpHeaders.PRAGMA, "no-cache").body(ApiResponse.success(user));
    }
}