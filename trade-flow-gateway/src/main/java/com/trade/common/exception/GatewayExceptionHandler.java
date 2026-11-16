package com.trade.common.exception;

import com.trade.common.response.ApiResponse;
import com.trade.enums.gateway.GatewayErrorCode;

import io.netty.channel.ConnectTimeoutException;
import io.netty.handler.timeout.ReadTimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.webflux.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.ErrorResponse;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.PrematureCloseException;

import tools.jackson.databind.json.JsonMapper;

import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.nio.channels.UnresolvedAddressException;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

/**
 * 将网关自身抛出的异常转换为统一响应，保留实际 HTTP 错误状态。
 */
@Component
@Order(-2)
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayExceptionHandler.class);

    private final JsonMapper jsonMapper;

    public GatewayExceptionHandler(JsonMapper jsonMapper) {
        this.jsonMapper = Objects.requireNonNull(jsonMapper, "jsonMapper");
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable error) {
        return Mono.defer(() -> {

            ServerHttpResponse response = exchange.getResponse();

            // 响应已经开始发送时，继续传播异常，不能再替换响应内容。
            if (response.isCommitted()) {
                return Mono.error(error);
            }

            Throwable cause = Exceptions.unwrap(error);
            HttpStatusCode status = resolveStatus(cause);
            GatewayErrorCode errorCode = GatewayErrorCode.fromHttpStatus(status);
            ServerHttpRequest request = exchange.getRequest();

            if (status.is5xxServerError()) {
                LOGGER.error("Gateway request failed: requestId={}, method={}, path={}, status={}", request.getId(), request.getMethod(), request.getPath().value(), status.value(), cause);
            } else {
                LOGGER.warn("Gateway request rejected: requestId={}, method={}, path={}, status={}", request.getId(), request.getMethod(), request.getPath().value(), status.value());
            }

            ApiResponse<Void> apiResponse = ApiResponse.failure(errorCode.getCode(), errorCode.getMessage());
            byte[] body = jsonMapper.writeValueAsBytes(apiResponse);

            response.setStatusCode(status);

            // 保留框架异常附带的协议头，例如 405 响应中的 Allow。
            if (cause instanceof ErrorResponse errorResponse) {
                response.getHeaders().putAll(errorResponse.getHeaders());
            }

            HttpHeaders headers = response.getHeaders();

            // 错误响应使用新的 JSON 内容，移除可能遗留的响应体元数据。
            headers.remove(HttpHeaders.CONTENT_ENCODING);
            headers.remove(HttpHeaders.TRANSFER_ENCODING);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setContentLength(body.length);
            headers.setCacheControl("no-store");

            if (HttpMethod.HEAD.equals(request.getMethod())) {
                return response.setComplete();
            }

            return response.writeWith(Mono.fromSupplier(() -> response.bufferFactory().wrap(body)));
        });
    }

    private HttpStatusCode resolveStatus(Throwable error) {

        // 框架已经明确指定 HTTP 状态时，优先使用该状态。
        if (error instanceof ErrorResponse errorResponse) {
            return errorResponse.getStatusCode();
        }

        // 连接超时属于超时错误，必须放在普通连接失败的判断之前。
        if (error instanceof ConnectTimeoutException || error instanceof ReadTimeoutException || error instanceof TimeoutException) {
            return HttpStatus.GATEWAY_TIMEOUT;
        }

        if (error instanceof ConnectException || error instanceof UnknownHostException || error instanceof NoRouteToHostException || error instanceof UnresolvedAddressException) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }

        if (error instanceof PrematureCloseException) {
            return HttpStatus.BAD_GATEWAY;
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}