package com.trade.bootstrap;

import com.trade.config.AuthBootstrapProperties;
import com.trade.config.AuthBootstrapTaskConfiguration;
import com.trade.service.AuthBootstrapService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Map;

/**
 * 执行一次管理员初始化任务，成功返回 0，失败返回 1。
 */
public final class AuthBootstrapCommand {

    private static final Logger log = LoggerFactory.getLogger(AuthBootstrapCommand.class);

    private AuthBootstrapCommand() {
    }

    public static int execute(String[] args) {

        SpringApplication application = new SpringApplication(AuthBootstrapTaskConfiguration.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.setAdditionalProfiles("auth-bootstrap");
        application.setDefaultProperties(Map.of("spring.config.name", "auth-bootstrap"));

        try (ConfigurableApplicationContext context = application.run(args)) {

            AuthBootstrapProperties properties = context.getBean(AuthBootstrapProperties.class);
            AuthBootstrapService service = context.getBean(AuthBootstrapService.class);

            boolean created = service.initializeAdministrator(properties.getUsername(), properties.getPassword(), properties.getDisplayName());

            if (created) {
                log.info("管理员初始化任务完成：已创建首个管理员");
            } else {
                log.info("管理员初始化任务完成：已有管理员，本次跳过");
            }

            return 0;
        } catch (Exception exception) {
            log.error("管理员初始化任务失败", exception);
            return 1;
        }
    }
}