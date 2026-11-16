package com.trade.service;

import com.trade.model.auth.AuthUserProfile;
import com.trade.response.auth.AuthCurrentUserResponse;

/**
 * 加载当前用户资料，并提供本次请求的当前用户查询入口。
 */
public interface AuthCurrentUserService {

    AuthUserProfile loadProfile(Long userId);

    AuthCurrentUserResponse getCurrentUser();

}