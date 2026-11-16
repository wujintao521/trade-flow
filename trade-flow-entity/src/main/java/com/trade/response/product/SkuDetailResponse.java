package com.trade.response.product;



import com.trade.enums.product.SkuSaleStatus;

import java.math.BigDecimal;

/**
 * 商品详情接口响应。
 */
public record SkuDetailResponse(Long skuId, String skuCode, String skuName, BigDecimal salePrice, SkuSaleStatus saleStatus) {
}