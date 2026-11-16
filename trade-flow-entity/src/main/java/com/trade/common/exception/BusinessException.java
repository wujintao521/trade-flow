package com.trade.common.exception;

import java.util.Objects;

/**
 * 通用业务异常。
 *
 * <p>具体错误码由所属业务模块定义。
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int code;

    public BusinessException(int code, String message) {
        super(Objects.requireNonNull(message, "message"));
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}