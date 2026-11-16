package com.trade.service;

/**
 * 首个管理员初始化业务。
 */
public interface AuthBootstrapService {

    /**
     * 创建成功返回 true；已有管理员时返回 false。
     */
    boolean initializeAdministrator(String username, String rawPassword, String displayName);

}