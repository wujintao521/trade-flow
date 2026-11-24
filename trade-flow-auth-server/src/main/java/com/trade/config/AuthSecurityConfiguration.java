package com.trade.config;

import com.trade.security.AuthSecurityExceptionHandler;
import com.trade.service.security.CurrentUserJwtAuthenticationConverter;

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
 * 认证服务的密码登录和 JWT 请求认证配置。
 */
@Configuration(proxyBeanMethods = false)
public class AuthSecurityConfiguration {

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(DaoAuthenticationProvider daoAuthenticationProvider) {
        return new ProviderManager(daoAuthenticationProvider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtDecoder jwtDecoder, CurrentUserJwtAuthenticationConverter jwtConverter, AuthSecurityExceptionHandler errorHandler) throws Exception {

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