package com.trade.service.product;

import com.trade.response.product.SkuBatchQueryResponse;
import com.trade.response.product.SkuDetailResponse;

import java.util.List;

/**
 * 商品SKU业务接口。
 */
public interface ProductSkuService {

    SkuDetailResponse getDetail(Long skuId);

    SkuBatchQueryResponse batchQuery(List<Long> skuIds);

}