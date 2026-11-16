package com.trade.enums.auth;

/**
 * 认证业务错误码，前 3 位对应 HTTP 状态码。
 */
public enum AuthErrorCode {

    INVALID_LOGIN_REQUEST(40031),

    INVALID_BEARER_REQUEST(40032),

    LOGIN_FAILED(40131),

    INVALID_ACCESS_TOKEN(40132),

    ACCESS_DENIED(40331),

    AUTHENTICATION_UNAVAILABLE(50331);

    private final int code;

    AuthErrorCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}