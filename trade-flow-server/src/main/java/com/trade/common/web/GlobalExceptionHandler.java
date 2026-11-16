package com.trade.common.web;

import com.trade.common.exception.BusinessException;

import com.trade.common.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * 统一处理MVC接口异常。
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {

        HttpStatus status = HttpStatus.resolve(exception.getCode() / 100);

        if (status == null || !status.isError()) {
            LOGGER.error("业务错误码未遵守HTTP状态映射约定，code={}", exception.getCode(), exception);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.<Void>failure(50000, "系统异常，请稍后重试"));
        }

        if (status.is5xxServerError()) {
            LOGGER.error("业务处理发生系统级异常，code={}", exception.getCode(), exception);
            return ResponseEntity.status(status).body(ApiResponse.<Void>failure(exception.getCode(), "系统异常，请稍后重试"));
        }

        return ResponseEntity.status(status).body(ApiResponse.<Void>failure(exception.getCode(), exception.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception exception) {
        LOGGER.error("接口发生未处理异常", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.<Void>failure(50000, "系统异常，请稍后重试"));
    }

    @Override
    protected ResponseEntity<Object> createResponseEntity(Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        ApiResponse<Void> response = ApiResponse.failure(statusCode.value() * 100, frameworkMessage(statusCode));
        return new ResponseEntity<>(response, headers, statusCode);
    }

    private String frameworkMessage(HttpStatusCode statusCode) {
        return switch (statusCode.value()) {
            case 400 -> "请求参数格式不正确";
            case 404 -> "请求资源不存在";
            case 405 -> "请求方法不支持";
            case 406 -> "不支持请求指定的响应格式";
            case 413 -> "请求内容过大";
            case 415 -> "请求内容类型不支持";
            case 429 -> "请求过于频繁";
            default -> statusCode.is5xxServerError() ? "系统异常，请稍后重试" : "请求失败";
        };
    }
}