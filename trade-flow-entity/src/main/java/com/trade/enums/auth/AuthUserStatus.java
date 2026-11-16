package com.trade.enums.auth;

/**
 * 认证账号状态，与 auth_user.status 对应。
 */
public enum AuthUserStatus {

    DISABLED(0),

    ENABLED(1);

    private final int code;

    AuthUserStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}