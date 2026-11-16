package com.trade.service.impl;

import com.trade.common.exception.BusinessException;
import com.trade.dao.auth.AuthUserMapper;
import com.trade.enums.auth.AuthErrorCode;
import com.trade.enums.auth.AuthUserStatus;
import com.trade.model.auth.AuthUserProfile;
import com.trade.response.auth.AuthCurrentUserResponse;
import com.trade.security.AuthCurrentUser;
import com.trade.service.AuthCurrentUserService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 根据已验证的用户 ID 加载资料，业务调用从当前请求上下文读取结果。
 */
@Service
public class AuthCurrentUserServiceImpl implements AuthCurrentUserService {

    private final AuthUserMapper authUserMapper;

    public AuthCurrentUserServiceImpl(AuthUserMapper authUserMapper) {
        this.authUserMapper = Objects.requireNonNull(authUserMapper, "authUserMapper");
    }

    @Override
    @Transactional(readOnly = true)
    public AuthUserProfile loadProfile(Long userId) {

        AuthUserProfile profile = authUserMapper.selectProfileById(userId);

        if (profile == null || !Integer.valueOf(AuthUserStatus.ENABLED.getCode()).equals(profile.status())) {
            throw new InvalidBearerTokenException("用户不存在或账号不可用");
        }

        return profile;
    }

    @Override
    public AuthCurrentUserResponse getCurrentUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof AuthCurrentUser user)) {
            throw new BusinessException(AuthErrorCode.INVALID_ACCESS_TOKEN.getCode(), "未登录或访问令牌无效");
        }

        return new AuthCurrentUserResponse(user.userId(), user.username(), user.displayName(), user.roles());
    }
}