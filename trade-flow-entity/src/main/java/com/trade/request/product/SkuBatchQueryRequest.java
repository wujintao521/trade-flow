package com.trade.request.product;

import java.util.List;

/**
 * 商品批量查询请求。
 */
public record SkuBatchQueryRequest(List<Long> skuIds) {
}