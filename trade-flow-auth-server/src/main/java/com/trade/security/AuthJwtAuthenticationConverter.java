package com.trade.security;

import com.trade.model.auth.AuthUserProfile;
import com.trade.service.AuthCurrentUserService;

import org.springframework.core.convert.converter.Converter;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * JWT 验证通过后，加载业务用户资料并创建认证结果。
 */
@Component
public class AuthJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final AuthCurrentUserService authCurrentUserService;

    public AuthJwtAuthenticationConverter(AuthCurrentUserService authCurrentUserService) {
        this.authCurrentUserService = Objects.requireNonNull(authCurrentUserService, "authCurrentUserService");
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {

        Long userId = parseUserId(jwt.getSubject());
        List<String> roles = readRoles(jwt);
        AuthUserProfile profile;

        try {
            profile = authCurrentUserService.loadProfile(userId);
        } catch (DataAccessException | TransactionException exception) {
            throw new AuthenticationServiceException("加载当前用户资料失败", exception);
        }

        AuthCurrentUser user = new AuthCurrentUser(profile.userId(), profile.username(), profile.displayName(), roles);
        List<GrantedAuthority> authorities = new ArrayList<>(roles.size());

        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        }

        return new AuthJwtAuthenticationToken(jwt, user, authorities);
    }

    private Long parseUserId(String subject) {

        try {
            long userId = Long.parseLong(subject);

            if (userId <= 0) {
                throw new NumberFormatException("用户 ID 必须为正整数");
            }

            return userId;
        } catch (NumberFormatException exception) {
            throw new InvalidBearerTokenException("访问令牌的用户标识无效");
        }
    }

    private List<String> readRoles(Jwt jwt) {

        Object claim = jwt.getClaims().get("roles");

        if (claim == null) {
            return List.of();
        }

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