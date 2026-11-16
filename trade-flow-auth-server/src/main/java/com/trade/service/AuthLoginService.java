package com.trade.service;

import com.trade.request.auth.AuthLoginRequest;
import com.trade.response.auth.AccessTokenResponse;

/**
 * 账号密码登录业务。
 */
public interface AuthLoginService {

    AccessTokenResponse login(AuthLoginRequest request);
}