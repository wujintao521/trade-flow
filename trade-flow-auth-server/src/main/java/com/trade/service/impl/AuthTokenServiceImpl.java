package com.trade.service.impl;

import com.trade.config.AuthJwtProperties;
import com.trade.response.auth.AccessTokenResponse;
import com.trade.security.AuthUserDetails;
import com.trade.service.AuthTokenService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 使用认证成功后的用户信息签发短期 JWT 访问令牌。
 */
@Service
public class AuthTokenServiceImpl implements AuthTokenService {

    private static final String ROLE_PREFIX = "ROLE_";

    private final JwtEncoder jwtEncoder;

    private final AuthJwtProperties properties;

    public AuthTokenServiceImpl(JwtEncoder jwtEncoder, AuthJwtProperties properties) {
        this.jwtEncoder = Objects.requireNonNull(jwtEncoder, "jwtEncoder");
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    @Override
    public AccessTokenResponse issueAccessToken(Authentication authentication) {

        Objects.requireNonNull(authentication, "authentication");

        if (!authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof AuthUserDetails user)) {
            throw new IllegalArgumentException("仅允许为已通过账号认证的用户签发访问令牌");
        }

        List<String> roleCodes = new ArrayList<>();

        for (GrantedAuthority authority : user.getAuthorities()) {

            String authorityName = authority.getAuthority();

            if (authorityName != null && authorityName.startsWith(ROLE_PREFIX)) {
                roleCodes.add(authorityName.substring(ROLE_PREFIX.length()));
            }
        }

        Instant issuedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant expiresAt = issuedAt.plus(properties.accessTokenTtl());

        JwtClaimsSet.Builder builder = JwtClaimsSet.builder();
        builder.issuer(properties.issuer());
        builder.subject(user.getUserId().toString());
        builder.audience(List.of(properties.audience()));
        builder.issuedAt(issuedAt);
        builder.expiresAt(expiresAt);
        builder.id(UUID.randomUUID().toString());
        builder.claim("username", user.getUsername());
        builder.claim("roles", roleCodes);

        JwtClaimsSet claims = builder.build();
        String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        return new AccessTokenResponse(accessToken, "Bearer", properties.accessTokenTtl().toSeconds());
    }
}