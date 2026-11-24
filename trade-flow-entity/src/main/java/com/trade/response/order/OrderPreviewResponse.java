package com.trade.response.order;

import java.math.BigDecimal;
import java.util.List;

/**
 * 本次订单预览结果，不代表订单已经创建或库存已经预占。
 */
public record OrderPreviewResponse(Long buyerId, BigDecimal totalAmount, String currency, List<OrderPreviewItemResponse> items) {

    public OrderPreviewResponse {
        items = List.copyOf(items);
    }
}