package com.trade.enums.order;

/**
 * 订单业务错误码。
 */
public enum OrderErrorCode {

    /**
     * 订单请求参数不合法。
     */
    INVALID_ORDER_REQUEST(40001),

    /**
     * 订单金额超出允许范围。
     */
    ORDER_AMOUNT_EXCEEDED(40002),

    /**
     * 订单包含不存在或不可售的商品。
     */
    ORDER_SKU_UNAVAILABLE(40901),

    /**
     * 服务端提供的订单定价数据异常。
     */
    ORDER_PRICING_DATA_INVALID(50001);

    private final int code;

    OrderErrorCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}