package com.trade.response.order;

import java.math.BigDecimal;

/**
 * 单条订单预览明细，价格和商品信息来自服务端。
 */
public record OrderPreviewItemResponse(Long skuId, String skuCode, String skuName, BigDecimal unitPrice, int quantity, BigDecimal lineAmount) {
}