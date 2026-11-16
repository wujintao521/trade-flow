package com.trade.model.order;

import java.math.BigDecimal;

/**
 * 订单业务需要的商品事实。
 *
 * <p>由服务端商品查询结果构建，不作为客户端请求对象。
 * saleable 表示商品是否允许购买，不表示库存一定充足。
 */
public record OrderSkuSnapshot(Long skuId, String skuCode, String skuName, BigDecimal unitPrice, boolean saleable) {
}