package com.trade.service.product.impl;

import com.trade.common.exception.BusinessException;
import com.trade.dao.product.ProductSkuMapper;
import com.trade.entity.product.ProductSku;
import com.trade.enums.product.ProductErrorCode;
import com.trade.response.product.SkuBatchQueryResponse;
import com.trade.response.product.SkuDetailResponse;
import com.trade.service.product.ProductSkuService;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 上架商品查询业务。
 */
@Service
public class ProductSkuServiceImpl implements ProductSkuService {

    /**
     * 限制原始请求规模，包括重复 ID。
     */
    private static final int MAX_INPUT_IDS = 100;

    /**
     * 限制一次实际查询的不同 SKU 数量。
     */
    private static final int MAX_DISTINCT_SKUS = 50;

    private final ProductSkuMapper productSkuMapper;

    public ProductSkuServiceImpl(ProductSkuMapper productSkuMapper) {
        this.productSkuMapper = Objects.requireNonNull(productSkuMapper, "productSkuMapper");
    }

    @Override
    public SkuDetailResponse getDetail(Long skuId) {

        if (skuId == null || skuId <= 0) {
            throw new BusinessException(ProductErrorCode.INVALID_SKU_ID.getCode(), "SKU ID 必须为正整数");
        }

        ProductSku productSku = productSkuMapper.selectOnSaleById(skuId);

        if (productSku == null) {
            throw new BusinessException(ProductErrorCode.SKU_NOT_FOUND.getCode(), "商品不存在或已下架");
        }

        return toResponse(productSku);
    }

    @Override
    public SkuBatchQueryResponse batchQuery(List<Long> skuIds) {

        List<Long> distinctSkuIds = validateAndDeduplicateSkuIds(skuIds);
        List<ProductSku> productSkus = productSkuMapper.selectOnSaleByIds(distinctSkuIds);

        List<SkuDetailResponse> items = new ArrayList<>(productSkus.size());

        for (ProductSku productSku : productSkus) {
            items.add(toResponse(productSku));
        }

        return new SkuBatchQueryResponse(items);
    }

    private List<Long> validateAndDeduplicateSkuIds(List<Long> skuIds) {

        if (skuIds == null || skuIds.isEmpty()) {
            throw invalidBatchQuery("SKU ID 列表不能为空");
        }

        if (skuIds.size() > MAX_INPUT_IDS) {
            throw invalidBatchQuery("原始 SKU ID 数量不能超过 " + MAX_INPUT_IDS + " 条");
        }

        Set<Long> distinctSkuIds = new HashSet<>();

        for (Long skuId : skuIds) {

            if (skuId == null || skuId <= 0) {
                throw invalidBatchQuery("SKU ID 必须为正整数");
            }

            distinctSkuIds.add(skuId);
        }

        if (distinctSkuIds.size() > MAX_DISTINCT_SKUS) {
            throw invalidBatchQuery("单次最多查询 " + MAX_DISTINCT_SKUS + " 种 SKU");
        }

        return new ArrayList<>(distinctSkuIds);
    }

    private SkuDetailResponse toResponse(ProductSku productSku) {
        return new SkuDetailResponse(productSku.id(), productSku.skuCode(), productSku.skuName(), productSku.salePrice(), productSku.saleStatus());
    }

    private BusinessException invalidBatchQuery(String message) {
        return new BusinessException(ProductErrorCode.INVALID_BATCH_QUERY.getCode(), message);
    }
}