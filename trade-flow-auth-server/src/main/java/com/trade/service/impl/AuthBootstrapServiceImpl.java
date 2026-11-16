package com.trade.service.impl;

import com.trade.dao.auth.AuthBootstrapMapper;
import com.trade.model.auth.AuthBootstrapUser;
import com.trade.service.AuthBootstrapService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 使用数据库事务完成首个管理员及其角色关系的初始化。
 */
@Service
public class AuthBootstrapServiceImpl implements AuthBootstrapService {

    private static final String ADMIN_ROLE_CODE = "ADMIN";

    private static final Pattern USERNAME_PATTERN = Pattern.compile("[a-z][a-z0-9._-]{2,63}");

    private static final int MIN_PASSWORD_CHARACTERS = 6;

    private static final int MAX_PASSWORD_BYTES = 72;

    private static final int MAX_DISPLAY_NAME_CHARACTERS = 64;

    private final AuthBootstrapMapper authBootstrapMapper;

    private final PasswordEncoder passwordEncoder;

    public AuthBootstrapServiceImpl(AuthBootstrapMapper authBootstrapMapper, PasswordEncoder passwordEncoder) {
        this.authBootstrapMapper = Objects.requireNonNull(authBootstrapMapper, "authBootstrapMapper");
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder, "passwordEncoder");
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public boolean initializeAdministrator(String username, String rawPassword, String displayName) {

        // ADMIN 角色由 V1 脚本预先创建，锁定该行以串行处理并发初始化。
        Long adminRoleId = authBootstrapMapper.selectRoleIdForUpdate(ADMIN_ROLE_CODE);

        if (adminRoleId == null) {
            throw new IllegalStateException("ADMIN 角色不存在，请检查认证数据库迁移结果");
        }

        // 已存在管理员关联时直接跳过，包括账号已禁用的情况。
        if (authBootstrapMapper.existsUserByRoleId(adminRoleId)) {
            return false;
        }

        String normalizedUsername = normalizeUsername(username);
        String normalizedDisplayName = normalizeDisplayName(displayName);
        validatePassword(rawPassword);

        if (authBootstrapMapper.existsUserByUsername(normalizedUsername)) {
            throw new IllegalStateException("初始化管理员用户名已被其他账号占用");
        }

        String passwordHash = passwordEncoder.encode(rawPassword);
        AuthBootstrapUser user = new AuthBootstrapUser(normalizedUsername, passwordHash, normalizedDisplayName);
        int insertedUsers = authBootstrapMapper.insertBootstrapUser(user);

        if (insertedUsers != 1 || user.getId() == null || user.getId() <= 0) {
            throw new IllegalStateException("初始化管理员账号失败");
        }

        int insertedRelations = authBootstrapMapper.insertUserRole(user.getId(), adminRoleId);

        if (insertedRelations != 1) {
            throw new IllegalStateException("初始化管理员角色关系失败");
        }

        return true;
    }

    private String normalizeUsername(String username) {

        if (username == null) {
            throw new IllegalArgumentException("请配置初始化管理员用户名");
        }

        String normalizedUsername = username.strip().toLowerCase(Locale.ROOT);

        if (!USERNAME_PATTERN.matcher(normalizedUsername).matches()) {
            throw new IllegalArgumentException("管理员用户名必须为 3 到 64 位，以英文字母开头，只能包含英文字母、数字、点、下划线或短横线");
        }

        return normalizedUsername;
    }

    private String normalizeDisplayName(String displayName) {

        if (displayName == null) {
            throw new IllegalArgumentException("请配置初始化管理员显示名称");
        }

        String normalizedDisplayName = displayName.strip();

        if (normalizedDisplayName.isEmpty() || normalizedDisplayName.codePointCount(0, normalizedDisplayName.length()) > MAX_DISPLAY_NAME_CHARACTERS) {
            throw new IllegalArgumentException("管理员显示名称必须为 1 到 64 个字符");
        }

        return normalizedDisplayName;
    }

    private void validatePassword(String rawPassword) {

        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("请配置初始化管理员密码");
        }

        if (rawPassword.codePointCount(0, rawPassword.length()) < MIN_PASSWORD_CHARACTERS) {
            throw new IllegalArgumentException("管理员密码至少需要 6 个字符");
        }

        if (rawPassword.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
            throw new IllegalArgumentException("管理员密码按 UTF-8 编码后不能超过 72 字节");
        }
    }
}