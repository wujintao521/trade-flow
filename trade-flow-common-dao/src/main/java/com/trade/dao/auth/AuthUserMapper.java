package com.trade.dao.auth;

import com.trade.entity.auth.AuthUser;

import com.trade.model.auth.AuthUserProfile;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 登录认证所需的账号及角色查询。
 */
public interface AuthUserMapper {

    AuthUser selectByUsername(@Param("username") String username);

    AuthUserProfile selectProfileById(@Param("userId") Long userId);

    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);
}