package com.trade.config;

import com.trade.security.ServerSecurityExceptionHandler;

import jakarta.servlet.DispatcherType;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 业务服务独立校验 JWT，并保护已有商品查询接口。
 */
@Configuration(proxyBeanMethods = false)
public class ServerSecurityConfiguration {

    @Bean
    public SecurityFilterChain serverSecurityFilterChain(HttpSecurity http, JwtDecoder serverJwtDecoder, ServerSecurityExceptionHandler errorHandler) throws Exception {

        http.csrf(csrf -> csrf.disable());
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.requestCache(cache -> cache.disable());
        http.formLogin(form -> form.disable());
        http.httpBasic(basic -> basic.disable());
        http.logout(logout -> logout.disable());

        http.authorizeHttpRequests(authorize -> {

            // 允许容器内部的错误分派，避免把原始错误覆盖成权限错误。
            authorize.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll();

            authorize.requestMatchers(HttpMethod.GET, "/internal/actuator/health", "/internal/actuator/health/liveness", "/internal/actuator/health/readiness").permitAll();

            authorize.requestMatchers(HttpMethod.GET, "/api/products/skus/{skuId}").authenticated();

            authorize.requestMatchers(HttpMethod.POST, "/api/products/skus/batch-query").authenticated();

            authorize.anyRequest().denyAll();
        });

        // 将 JWT 中的 ADMIN 转换为 Spring Security 使用的 ROLE_ADMIN。
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("roles");
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

        http.oauth2ResourceServer(resourceServer -> {

            resourceServer.jwt(jwt -> jwt.decoder(serverJwtDecoder).jwtAuthenticationConverter(jwtConverter));

            resourceServer.authenticationEntryPoint(errorHandler);
            resourceServer.accessDeniedHandler(errorHandler);

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