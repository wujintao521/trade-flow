package com.trade.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

import java.time.Duration;
import java.util.Objects;

/**
 * JWT 签发配置，密钥文件通过运行环境提供。
 */
@ConfigurationProperties(prefix = "trade.auth.jwt")
public record AuthJwtProperties(String issuer, String audience, String keyId, Duration accessTokenTtl, Resource privateKeyLocation, Resource publicKeyLocation) {

    public AuthJwtProperties {

        if (issuer == null || issuer.isBlank()) {
            throw new IllegalArgumentException("请配置 JWT 签发方 issuer");
        }

        if (audience == null || audience.isBlank()) {
            throw new IllegalArgumentException("请配置 JWT 接收方 audience");
        }

        if (keyId == null || keyId.isBlank()) {
            throw new IllegalArgumentException("请配置 JWT 密钥标识 key-id");
        }

        if (accessTokenTtl == null || accessTokenTtl.isZero() || accessTokenTtl.isNegative() || accessTokenTtl.getNano() != 0) {
            throw new IllegalArgumentException("JWT 有效期必须为正整数秒");
        }

        Objects.requireNonNull(privateKeyLocation, "请配置 JWT 私钥路径");
        Objects.requireNonNull(publicKeyLocation, "请配置 JWT 公钥路径");
    }
}