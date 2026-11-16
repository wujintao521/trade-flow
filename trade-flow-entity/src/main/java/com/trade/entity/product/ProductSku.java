package com.trade.entity.product;



import com.trade.enums.product.SkuSaleStatus;

import java.math.BigDecimal;

/**
 * 商品SKU查询实体。
 */
public record ProductSku(Long id, String skuCode, String skuName, BigDecimal salePrice, SkuSaleStatus saleStatus) {
}