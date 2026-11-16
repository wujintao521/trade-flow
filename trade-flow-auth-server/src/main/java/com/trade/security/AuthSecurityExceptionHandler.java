package com.trade.security;

import com.trade.common.response.ApiResponse;
import com.trade.enums.auth.AuthErrorCode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.resource.BearerTokenError;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * 统一处理安全过滤链异常，返回 ApiResponse JSON。
 */
@Component
public class AuthSecurityExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthSecurityExceptionHandler.class);

    private final ObjectMapper objectMapper;

    public AuthSecurityExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {

        if (exception instanceof AuthenticationServiceException) {
            LOGGER.error("JWT 认证依赖暂时不可用", exception);
            writeFailure(response, HttpStatus.SERVICE_UNAVAILABLE, AuthErrorCode.AUTHENTICATION_UNAVAILABLE.getCode(), "认证服务暂时不可用，请稍后重试");
            return;
        }

        if (exception instanceof OAuth2AuthenticationException oauthException && oauthException.getError() instanceof BearerTokenError error) {

            if (error.getHttpStatus() == HttpStatus.BAD_REQUEST) {
                response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer error=\"invalid_request\"");
                writeFailure(response, HttpStatus.BAD_REQUEST, AuthErrorCode.INVALID_BEARER_REQUEST.getCode(), "访问令牌请求格式不正确");
                return;
            }

            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer error=\"invalid_token\"");
        } else {
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        }

        writeFailure(response, HttpStatus.UNAUTHORIZED, AuthErrorCode.INVALID_ACCESS_TOKEN.getCode(), "未登录或访问令牌无效");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception) throws IOException {
        writeFailure(response, HttpStatus.FORBIDDEN, AuthErrorCode.ACCESS_DENIED.getCode(), "无权访问该资源");
    }

    private void writeFailure(HttpServletResponse response, HttpStatus status, int code, String message) throws IOException {

        if (response.isCommitted()) {
            return;
        }

        response.setStatus(status.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        response.setHeader(HttpHeaders.PRAGMA, "no-cache");
        objectMapper.writeValue(response.getOutputStream(), ApiResponse.<Void>failure(code, message));
    }
}