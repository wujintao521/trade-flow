package com.trade.enums.product;

/**
 * 商品业务错误码。
 */
public enum ProductErrorCode {

    INVALID_SKU_ID(40011),

    INVALID_BATCH_QUERY(40012),

    SKU_NOT_FOUND(40411);

    private final int code;

    ProductErrorCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}