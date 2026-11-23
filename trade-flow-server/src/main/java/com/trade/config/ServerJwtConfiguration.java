package com.trade.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.io.IOException;
import java.io.InputStream;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * 创建业务服务的 JWT 验证组件。
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ServerJwtProperties.class)
public class ServerJwtConfiguration {

    @Bean
    public RSAPublicKey serverJwtPublicKey(ServerJwtProperties properties) throws IOException {

        RSAPublicKey publicKey;

        try (InputStream input = properties.publicKeyLocation().getInputStream()) {
            publicKey = RsaKeyConverters.x509().convert(input);
        }

        if (publicKey == null || publicKey.getModulus().bitLength() < 2048) {
            throw new IllegalArgumentException("JWT RSA 公钥解析失败或长度小于 2048 位");
        }

        return publicKey;
    }

    @Bean
    public JwtDecoder serverJwtDecoder(ServerJwtProperties properties, RSAPublicKey serverJwtPublicKey) {

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(serverJwtPublicKey).signatureAlgorithm(SignatureAlgorithm.RS256).build();

        JwtClaimValidator<List<String>> audienceValidator = new JwtClaimValidator<>(JwtClaimNames.AUD, audience -> audience != null && audience.contains(properties.audience()));

        JwtClaimValidator<Instant> expirationRequired = new JwtClaimValidator<>(JwtClaimNames.EXP, Objects::nonNull);

        JwtClaimValidator<String> subjectValidator = new JwtClaimValidator<>(JwtClaimNames.SUB, this::isValidUserId);

        JwtClaimValidator<Object> rolesValidator = new JwtClaimValidator<>("roles", this::isValidRoles);

        DelegatingOAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefaultWithIssuer(properties.issuer()), audienceValidator, expirationRequired, subjectValidator, rolesValidator);

        decoder.setJwtValidator(validator);

        return decoder;
    }

    private boolean isValidUserId(String subject) {

        if (subject == null || !subject.matches("[1-9][0-9]*")) {
            return false;
        }

        try {
            return Long.parseLong(subject) > 0;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private boolean isValidRoles(Object claim) {

        if (!(claim instanceof List<?> roles)) {
            return false;
        }

        for (Object role : roles) {

            if (!(role instanceof String roleCode) || roleCode.isBlank()) {
                return false;
            }
        }

        return true;
    }

}