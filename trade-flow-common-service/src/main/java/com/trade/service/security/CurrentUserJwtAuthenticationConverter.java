package com.trade.service.security;

import com.trade.model.auth.AuthUserProfile;
import com.trade.model.security.CurrentUser;

import org.springframework.core.convert.converter.Converter;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.FactorGrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * JWT 验证通过后，统一加载用户资料并创建公共认证身份。
 */
@Component
public class CurrentUserJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final CurrentUserService currentUserService;

    public CurrentUserJwtAuthenticationConverter(CurrentUserService currentUserService) {
        this.currentUserService = Objects.requireNonNull(currentUserService, "currentUserService");
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {

        Long userId = parseUserId(jwt.getSubject());

        Object usernameClaim = jwt.getClaims().get("username");

        if (!(usernameClaim instanceof String username) || username.isBlank()) {
            throw new InvalidBearerTokenException("访问令牌的用户名无效");
        }

        List<String> roles = readRoles(jwt);

        AuthUserProfile profile;

        try {
            profile = currentUserService.loadProfile(userId);
        } catch (DataAccessException | TransactionException exception) {
            throw new AuthenticationServiceException("加载当前用户资料失败", exception);
        }

        // 用户名和昵称使用数据库中的最新资料。
        CurrentUser user = new CurrentUser(profile.userId(), profile.username(), profile.displayName(), roles);

        List<GrantedAuthority> authorities = new ArrayList<>(roles.size() + 1);

        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        }

        // 保留框架默认附加的 Bearer 认证标识，它不是业务角色。
        authorities.add(FactorGrantedAuthority.fromAuthority(FactorGrantedAuthority.BEARER_AUTHORITY));

        return new CurrentUserJwtAuthenticationToken(jwt, user, authorities);
    }

    private Long parseUserId(String subject) {

        if (subject == null || !subject.matches("[1-9][0-9]*")) {
            throw new InvalidBearerTokenException("访问令牌的用户标识无效");
        }

        try {
            return Long.parseLong(subject);
        } catch (NumberFormatException exception) {
            throw new InvalidBearerTokenException("访问令牌的用户标识无效");
        }
    }

    private List<String> readRoles(Jwt jwt) {

        Object claim = jwt.getClaims().get("roles");

        if (!(claim instanceof List<?> values)) {
            throw new InvalidBearerTokenException("访问令牌的角色信息无效");
        }

        List<String> roles = new ArrayList<>(values.size());

        for (Object value : values) {

            if (!(value instanceof String role) || role.isBlank()) {
                throw new InvalidBearerTokenException("访问令牌的角色信息无效");
            }

            roles.add(role);
        }

        return List.copyOf(roles);
    }
}