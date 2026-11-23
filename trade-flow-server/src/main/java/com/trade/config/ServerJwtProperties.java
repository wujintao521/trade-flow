package com.trade.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

import java.util.Objects;

/**
 * 业务服务只读取验签公钥，不持有签发 Token 的私钥。
 */
@ConfigurationProperties(prefix = "trade.security.jwt")
public record ServerJwtProperties(String issuer, String audience, Resource publicKeyLocation) {

    public ServerJwtProperties {

        if (issuer == null || issuer.isBlank()) {
            throw new IllegalArgumentException("请配置 JWT 签发方 issuer");
        }

        if (audience == null || audience.isBlank()) {
            throw new IllegalArgumentException("请配置 JWT 接收方 audience");
        }

        Objects.requireNonNull(publicKeyLocation, "请配置 JWT 公钥路径");
    }
}