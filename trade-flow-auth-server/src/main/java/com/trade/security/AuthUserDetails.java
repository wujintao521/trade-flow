package com.trade.security;

import com.trade.entity.auth.AuthUser;
import com.trade.enums.auth.AuthUserStatus;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * Spring Security 使用的认证用户信息，补充用户 ID 和显示名称。
 */
public final class AuthUserDetails extends User {

    private static final long serialVersionUID = 1L;

    private final Long userId;

    private final String displayName;

    public AuthUserDetails(AuthUser user, Collection<? extends GrantedAuthority> authorities) {
        // 当前账号表只管理启用状态，尚未设置账号到期、锁定或密码到期规则。
        super(user.getUsername(), user.getPasswordHash(), user.getStatus() == AuthUserStatus.ENABLED.getCode(), true, true, true, authorities);
        this.userId = user.getId();
        this.displayName = user.getDisplayName();
    }

    public Long getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }
}