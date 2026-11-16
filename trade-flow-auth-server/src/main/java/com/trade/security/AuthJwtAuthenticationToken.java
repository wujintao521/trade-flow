package com.trade.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;

import java.util.Collection;
import java.util.Map;

/**
 * JWT 认证结果，principal 保存本次请求的业务用户信息。
 */
public final class AuthJwtAuthenticationToken extends AbstractOAuth2TokenAuthenticationToken<Jwt> {

    private static final long serialVersionUID = 1L;

    public AuthJwtAuthenticationToken(Jwt jwt, AuthCurrentUser user, Collection<? extends GrantedAuthority> authorities) {
        super(jwt, user, jwt, authorities);
        setAuthenticated(true);
    }

    @Override
    public AuthCurrentUser getPrincipal() {
        return (AuthCurrentUser) super.getPrincipal();
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