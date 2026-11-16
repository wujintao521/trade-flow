package com.trade.model.auth;

import java.util.Objects;

/**
 * 首个管理员的用户写入数据，ID 由 MySQL 生成后回填。
 */
public class AuthBootstrapUser {

    private Long id;

    private final String username;

    private final String passwordHash;

    private final String displayName;

    public AuthBootstrapUser(String username, String passwordHash, String displayName) {
        this.username = Objects.requireNonNull(username, "username");
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash");
        this.displayName = Objects.requireNonNull(displayName, "displayName");
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }
}