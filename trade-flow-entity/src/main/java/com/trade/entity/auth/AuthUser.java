package com.trade.entity.auth;

import java.util.Objects;

/**
 * 认证账号查询实体，仅供服务内部使用，不作为接口响应返回。
 */
public final class AuthUser {

    private final Long id;

    private final String username;

    private final String passwordHash;

    private final String displayName;

    private final Integer status;

    public AuthUser(Long id, String username, String passwordHash, String displayName, Integer status) {
        this.id = Objects.requireNonNull(id, "id");
        this.username = Objects.requireNonNull(username, "username");
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash");
        this.displayName = Objects.requireNonNull(displayName, "displayName");
        this.status = Objects.requireNonNull(status, "status");
    }

    public Long getId() {
        return id;
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

    public Integer getStatus() {
        return status;
    }
}