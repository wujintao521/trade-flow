package com.trade.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import java.io.IOException;
import java.io.InputStream;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * 读取 RSA 密钥，创建使用 RS256 签名的 JWT 编码组件。
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AuthJwtProperties.class)
public class AuthJwtConfiguration {

    @Bean
    public RSAPublicKey jwtPublicKey(AuthJwtProperties properties) throws IOException {

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
    public JwtEncoder jwtEncoder(AuthJwtProperties properties, RSAPublicKey jwtPublicKey) throws IOException {

        RSAPrivateKey privateKey;

        try (InputStream input = properties.privateKeyLocation().getInputStream()) {
            privateKey = RsaKeyConverters.pkcs8().convert(input);
        }

        if (privateKey == null || !jwtPublicKey.getModulus().equals(privateKey.getModulus())) {
            throw new IllegalArgumentException("JWT 私钥解析失败或与公钥不匹配");
        }

        NimbusJwtEncoder.RsaKeyPairJwtEncoderBuilder builder = NimbusJwtEncoder.withKeyPair(jwtPublicKey, privateKey);
        builder.algorithm(SignatureAlgorithm.RS256);
        builder.jwkPostProcessor(keyBuilder -> keyBuilder.keyID(properties.keyId()));
        return builder.build();
    }

    @Bean
    public JwtDecoder jwtDecoder(AuthJwtProperties properties, RSAPublicKey jwtPublicKey) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(jwtPublicKey).signatureAlgorithm(SignatureAlgorithm.RS256).build();
        JwtClaimValidator<List<String>> audienceValidator = new JwtClaimValidator<>(JwtClaimNames.AUD, audience -> audience != null && audience.contains(properties.audience()));
        JwtClaimValidator<Instant> expirationRequired = new JwtClaimValidator<>(JwtClaimNames.EXP, Objects::nonNull);
        DelegatingOAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefaultWithIssuer(properties.issuer()), audienceValidator, expirationRequired);
        decoder.setJwtValidator(validator);
        return decoder;
    }
}