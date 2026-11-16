package com.trade.enums.gateway;

import org.springframework.http.HttpStatusCode;

/**
 * 网关自身产生的错误码。
 */
public enum GatewayErrorCode {

    REQUEST_REJECTED(40090, "网关拒绝了当前请求"),

    RESOURCE_NOT_FOUND(40490, "请求的资源不存在"),

    BAD_GATEWAY(50290, "下游服务响应异常"),

    SERVICE_UNAVAILABLE(50390, "服务暂时不可用，请稍后重试"),

    GATEWAY_TIMEOUT(50490, "下游服务响应超时"),

    INTERNAL_ERROR(50090, "网关处理请求失败");

    private final int code;

    private final String message;

    GatewayErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static GatewayErrorCode fromHttpStatus(HttpStatusCode status) {
        return switch (status.value()) {
            case 404 -> RESOURCE_NOT_FOUND;
            case 502 -> BAD_GATEWAY;
            case 503 -> SERVICE_UNAVAILABLE;
            case 504 -> GATEWAY_TIMEOUT;
            default -> status.is4xxClientError() ? REQUEST_REJECTED : INTERNAL_ERROR;
        };
    }
}