package com.trade.config;

import com.trade.security.AuthJwtAuthenticationConverter;
import com.trade.security.AuthSecurityExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 认证服务的账号密码认证配置。
 */
@Configuration(proxyBeanMethods = false)
public class AuthSecurityConfiguration {

    // 执行账号密码认证，调用用户加载服务和密码工具
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        // 需要查询用户信息时，调用传入的userDetailsService
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    // 认证入口接口
    @Bean
    public AuthenticationManager authenticationManager(DaoAuthenticationProvider daoAuthenticationProvider) {
        return new ProviderManager(daoAuthenticationProvider);
    }

    /**
     * 设置安全过滤链 规定接口如何访问 jwt如何验证 认证失败如何响应
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtDecoder jwtDecoder, AuthJwtAuthenticationConverter jwtConverter, AuthSecurityExceptionHandler errorHandler) throws Exception {

        // 当前接口仅从 Authorization 请求头接收 Bearer Token，不使用 Cookie 登录态。
        http.csrf(csrf -> csrf.disable());
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.requestCache(cache -> cache.disable());
        http.formLogin(form -> form.disable());
        http.httpBasic(basic -> basic.disable());
        http.logout(logout -> logout.disable());

        http.authorizeHttpRequests(authorize -> {
            authorize.requestMatchers(HttpMethod.POST, "/login").permitAll();
            authorize.requestMatchers(HttpMethod.GET, "/internal/actuator/health", "/internal/actuator/health/liveness", "/internal/actuator/health/readiness").permitAll();
            authorize.requestMatchers(HttpMethod.GET, "/me").authenticated();
            authorize.anyRequest().denyAll();
        });

        http.oauth2ResourceServer(resourceServer -> {
            resourceServer.jwt(jwt -> jwt.decoder(jwtDecoder).jwtAuthenticationConverter(jwtConverter));
            resourceServer.authenticationEntryPoint(errorHandler);
            resourceServer.accessDeniedHandler(errorHandler);

            // 认证依赖异常也交给统一处理器返回 503，不继续向 Servlet 容器抛出。
            resourceServer.withObjectPostProcessor(new ObjectPostProcessor<BearerTokenAuthenticationFilter>() {
                @Override
                public <O extends BearerTokenAuthenticationFilter> O postProcess(O filter) {
                    filter.setAuthenticationFailureHandler(errorHandler::commence);
                    return filter;
                }
            });
        });

        http.exceptionHandling(exceptions -> {
            exceptions.authenticationEntryPoint(errorHandler);
            exceptions.accessDeniedHandler(errorHandler);
        });

        return http.build();
    }
}