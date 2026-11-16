package com.trade.config;

import com.trade.service.impl.AuthBootstrapServiceImpl;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

/**
 * 一次性管理员初始化任务的 Spring 配置。
 */
@Configuration(proxyBeanMethods = false)
@Profile("auth-bootstrap")
@ConditionalOnNotWebApplication
@EnableAutoConfiguration
@EnableConfigurationProperties(AuthBootstrapProperties.class)
@MapperScan("com.trade.dao.auth")
@Import({AuthPasswordConfiguration.class, AuthBootstrapServiceImpl.class})
public class AuthBootstrapTaskConfiguration {
}