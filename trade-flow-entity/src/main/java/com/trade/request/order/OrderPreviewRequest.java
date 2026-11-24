package com.trade.request.order;

import java.util.List;

/**
 * 订单预览请求，只接收购买的 SKU 和数量。
 */
public record OrderPreviewRequest(List<OrderItemRequest> items) {
}