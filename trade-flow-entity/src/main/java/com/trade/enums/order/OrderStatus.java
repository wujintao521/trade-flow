package com.trade.enums.order;

/**
 * 订单业务状态。
 */
public enum OrderStatus {

    /**
     * 待支付。
     */
    PENDING_PAYMENT,

    /**
     * 已支付，等待履约。
     */
    PAID,

    /**
     * 履约中。
     */
    FULFILLING,

    /**
     * 已完成。
     */
    COMPLETED,

    /**
     * 已取消。
     */
    CANCELLED
}