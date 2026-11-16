package com.trade.web;

import com.trade.common.exception.BusinessException;
import com.trade.common.response.ApiResponse;
import com.trade.enums.auth.AuthErrorCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * 统一处理认证服务 MVC 接口异常。
 */
@RestControllerAdvice
public class AuthExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {

        HttpStatus status = HttpStatus.resolve(exception.getCode() / 100);

        if (status == null || !status.isError()) {
            LOGGER.error("业务错误码未遵守 HTTP 状态映射约定，code={}", exception.getCode(), exception);
            return failure(HttpStatus.INTERNAL_SERVER_ERROR, 50000, "系统异常，请稍后重试");
        }

        if (status.is5xxServerError()) {
            LOGGER.error("认证业务处理发生系统级异常，code={}", exception.getCode(), exception);
            return failure(status, exception.getCode(), "系统异常，请稍后重试");
        }

        return failure(status, exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler(AuthenticationServiceException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationServiceException(AuthenticationServiceException exception) {
        LOGGER.error("账号认证服务暂时不可用", exception);
        return failure(HttpStatus.SERVICE_UNAVAILABLE, AuthErrorCode.AUTHENTICATION_UNAVAILABLE.getCode(), "认证服务暂时不可用，请稍后重试");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException exception) {
        return failure(HttpStatus.UNAUTHORIZED, AuthErrorCode.LOGIN_FAILED.getCode(), "用户名或密码错误");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception exception) {
        LOGGER.error("认证接口发生未处理异常", exception);
        return failure(HttpStatus.INTERNAL_SERVER_ERROR, 50000, "系统异常，请稍后重试");
    }

    @Override
    protected ResponseEntity<Object> createResponseEntity(Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        ApiResponse<Void> response = ApiResponse.failure(statusCode.value() * 100, frameworkMessage(statusCode));
        return new ResponseEntity<>(response, responseHeaders(headers, statusCode), statusCode);
    }

    private ResponseEntity<ApiResponse<Void>> failure(HttpStatusCode status, int code, String message) {
        return new ResponseEntity<>(ApiResponse.<Void>failure(code, message), responseHeaders(new HttpHeaders(), status), status);
    }

    private HttpHeaders responseHeaders(HttpHeaders original, HttpStatusCode status) {

        HttpHeaders headers = new HttpHeaders();
        headers.putAll(original);
        headers.remove(HttpHeaders.CONTENT_LENGTH);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setCacheControl(CacheControl.noStore());
        headers.setPragma("no-cache");

        if (status.value() == HttpStatus.UNAUTHORIZED.value()) {
            headers.set(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        }

        return headers;
    }

    private String frameworkMessage(HttpStatusCode status) {
        return switch (status.value()) {
            case 400 -> "请求参数格式不正确";
            case 404 -> "请求资源不存在";
            case 405 -> "请求方法不支持";
            case 406 -> "不支持请求指定的响应格式";
            case 413 -> "请求内容过大";
            case 415 -> "请求内容类型不支持";
            case 429 -> "请求过于频繁";
            default -> status.is5xxServerError() ? "系统异常，请稍后重试" : "请求失败";
        };
    }
}