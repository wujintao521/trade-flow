package com.trade.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 一次性管理员初始化任务使用的账号配置。
 */
@ConfigurationProperties(prefix = "trade.auth.bootstrap")
public class AuthBootstrapProperties {

    private String username;

    private String password;

    private String displayName;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}