package com.trade.response.product;

import java.util.List;

/**
 * 商品批量查询响应。
 */
public record SkuBatchQueryResponse(List<SkuDetailResponse> items) {

    public SkuBatchQueryResponse {
        items = List.copyOf(items);
    }
}