package com.trade.controller;

import com.trade.common.response.ApiResponse;
import com.trade.model.security.CurrentUser;
import com.trade.response.auth.AuthCurrentUserResponse;
import com.trade.service.security.CurrentUserService;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * 通过公共服务读取当前登录用户的基本信息。
 */
@RestController
public class AuthCurrentUserController {

    private final CurrentUserService currentUserService;

    public AuthCurrentUserController(CurrentUserService currentUserService) {
        this.currentUserService = Objects.requireNonNull(currentUserService, "currentUserService");
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthCurrentUserResponse>> getCurrentUser() {
        CurrentUser user = currentUserService.getRequiredUser();
        AuthCurrentUserResponse response = new AuthCurrentUserResponse(user.userId(), user.username(), user.displayName(), user.roles());
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).header(HttpHeaders.PRAGMA, "no-cache").body(ApiResponse.success(response));
    }
}