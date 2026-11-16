package com.trade.dao.auth;

import com.trade.model.auth.AuthBootstrapUser;

import org.apache.ibatis.annotations.Param;

/**
 * 首个管理员初始化所需的数据访问。
 */
public interface AuthBootstrapMapper {

    Long selectRoleIdForUpdate(@Param("roleCode") String roleCode);

    boolean existsUserByRoleId(@Param("roleId") Long roleId);

    boolean existsUserByUsername(@Param("username") String username);

    int insertBootstrapUser(AuthBootstrapUser user);

    int insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

}