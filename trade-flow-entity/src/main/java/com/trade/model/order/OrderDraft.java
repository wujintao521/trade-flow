package com.trade.model.order;

import com.trade.enums.order.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

/**
 * 服务端生成的订单草稿。
 *
 * <p>用于承载计算后的订单信息。
 * 生成草稿不代表订单已落库、库存已预占或支付已完成。
 *
 * @param buyerId     下单用户 ID，由可信身份上下文取得
 * @param totalAmount 订单总金额
 * @param currency    币种，首版使用 CNY
 * @param status      订单状态，草稿生成时为 PENDING_PAYMENT
 * @param items       订单明细
 */
public record OrderDraft(
        Long buyerId,
        BigDecimal totalAmount,
        String currency,
        OrderStatus status,
        List<OrderItemDraft> items
) {

    /**
     * 防御性复制明细列表，避免调用方通过原列表修改草稿内容。
     */
    public OrderDraft {
        items = List.copyOf(items);
    }
}