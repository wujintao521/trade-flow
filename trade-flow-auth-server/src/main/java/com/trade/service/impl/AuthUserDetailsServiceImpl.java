package com.trade.service.impl;

import com.trade.dao.auth.AuthUserMapper;
import com.trade.entity.auth.AuthUser;
import com.trade.security.AuthUserDetails;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 从数据库加载 Spring Security 进行账号密码认证所需的用户信息。
 */
@Service
public class AuthUserDetailsServiceImpl implements UserDetailsService {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("[a-z][a-z0-9._-]{2,63}");

    private final AuthUserMapper authUserMapper;

    public AuthUserDetailsServiceImpl(AuthUserMapper authUserMapper) {
        this.authUserMapper = Objects.requireNonNull(authUserMapper, "authUserMapper");
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        String normalizedUsername = normalizeUsername(username);
        AuthUser user = authUserMapper.selectByUsername(normalizedUsername);

        if (user == null) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }

        List<String> roleCodes = authUserMapper.selectRoleCodesByUserId(user.getId());
        List<GrantedAuthority> authorities = new ArrayList<>(roleCodes.size());

        for (String roleCode : roleCodes) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + roleCode));
        }

        return new AuthUserDetails(user, authorities);
    }

    private String normalizeUsername(String username) {

        if (username == null) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }

        String normalizedUsername = username.strip().toLowerCase(Locale.ROOT);

        if (!USERNAME_PATTERN.matcher(normalizedUsername).matches()) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }

        return normalizedUsername;
    }
}