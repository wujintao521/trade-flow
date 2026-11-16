/*
package com.trade.bootstrap;

import com.trade.config.auth.AuthBootstrapProperties;
import com.trade.service.AuthBootstrapService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Objects;

*/
/**
 * 显式启用时，在应用启动阶段初始化首个管理员。
 *//*

@Component
@ConditionalOnProperty(prefix = "trade.auth.bootstrap", name = "enabled", havingValue = "true")
public class AuthBootstrapRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AuthBootstrapRunner.class);

    private final AuthBootstrapProperties properties;

    private final AuthBootstrapService authBootstrapService;

    public AuthBootstrapRunner(AuthBootstrapProperties properties, AuthBootstrapService authBootstrapService) {
        this.properties = Objects.requireNonNull(properties, "properties");
        this.authBootstrapService = Objects.requireNonNull(authBootstrapService, "authBootstrapService");
    }

    @Override
    public void run(ApplicationArguments args) {

        boolean created = authBootstrapService.initializeAdministrator(properties.getUsername(), properties.getPassword(), properties.getDisplayName());

        if (created) {
            log.info("首个管理员初始化成功，请关闭管理员初始化开关并移除初始化凭据");
        } else {
            log.info("已存在管理员，跳过首个管理员初始化");
        }
    }
}*/
