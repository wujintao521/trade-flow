package com.trade.service.security;

import com.trade.common.exception.BusinessException;
import com.trade.dao.auth.AuthUserMapper;
import com.trade.enums.auth.AuthErrorCode;
import com.trade.enums.auth.AuthUserStatus;
import com.trade.model.auth.AuthUserProfile;
import com.trade.model.security.CurrentUser;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 公共用户资料加载和当前请求身份读取入口。
 */
@Service
public class CurrentUserService {

    private final AuthUserMapper authUserMapper;

    public CurrentUserService(AuthUserMapper authUserMapper) {
        this.authUserMapper = Objects.requireNonNull(authUserMapper, "authUserMapper");
    }

    /**
     * 在 JWT 验证通过后，根据用户 ID 加载资料并检查账号状态。
     */
    @Transactional(readOnly = true)
    public AuthUserProfile loadProfile(Long userId) {

        if (userId == null || userId <= 0) {
            throw new InvalidBearerTokenException("访问令牌的用户标识无效");
        }

        AuthUserProfile profile = authUserMapper.selectProfileById(userId);

        if (profile == null || !Integer.valueOf(AuthUserStatus.ENABLED.getCode()).equals(profile.status())) {
            throw new InvalidBearerTokenException("用户不存在或账号不可用");
        }

        return profile;
    }

    /**
     * 从当前请求上下文读取用户对象，不再次查询数据库。
     */
    public CurrentUser getRequiredUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof CurrentUser user)) {
            throw new BusinessException(AuthErrorCode.INVALID_ACCESS_TOKEN.getCode(), "未登录或访问令牌无效");
        }

        return user;
    }

    public Long getRequiredUserId() {
        return getRequiredUser().userId();
    }
}