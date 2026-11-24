package com.trade.service.security;

import com.trade.model.security.CurrentUser;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;

import java.util.Collection;
import java.util.Map;

/**
 * 公共 JWT 认证结果，principal 保存 CurrentUser。
 */
public final class CurrentUserJwtAuthenticationToken extends AbstractOAuth2TokenAuthenticationToken<Jwt> {

    private static final long serialVersionUID = 1L;

    public CurrentUserJwtAuthenticationToken(Jwt jwt, CurrentUser user, Collection<? extends GrantedAuthority> authorities) {
        super(jwt, user, jwt, authorities);
        setAuthenticated(true);
    }

    @Override
    public CurrentUser getPrincipal() {
        return (CurrentUser) super.getPrincipal();
    }

    @Override
    public String getName() {
        return getPrincipal().userId().toString();
    }

    @Override
    public Map<String, Object> getTokenAttributes() {
        return getToken().getClaims();
    }
}