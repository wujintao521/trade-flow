package com.trade.request.auth;

/**
 * 账号密码登录请求，密码按原始内容参与校验。
 */
public record AuthLoginRequest(String username, String password) {

    @Override
    public String toString() {
        return "AuthLoginRequest[username=" + username + ", password=[PROTECTED]]";
    }
}