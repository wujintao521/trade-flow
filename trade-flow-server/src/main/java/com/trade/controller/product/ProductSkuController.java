package com.trade.controller.product;

import com.trade.common.response.ApiResponse;
import com.trade.request.product.SkuBatchQueryRequest;
import com.trade.response.product.SkuBatchQueryResponse;
import com.trade.response.product.SkuDetailResponse;
import com.trade.service.product.ProductSkuService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * 上架商品查询接口。
 */
@RestController
@RequestMapping("/api/products/skus")
public class ProductSkuController {

    private final ProductSkuService productSkuService;

    public ProductSkuController(ProductSkuService productSkuService) {
        this.productSkuService = Objects.requireNonNull(productSkuService, "productSkuService");
    }

    @GetMapping("/{skuId}")
    public ApiResponse<SkuDetailResponse> getDetail(@PathVariable("skuId") Long skuId) {
        return ApiResponse.success(productSkuService.getDetail(skuId));
    }

    @PostMapping("/batch-query")
    public ApiResponse<SkuBatchQueryResponse> batchQuery(@RequestBody SkuBatchQueryRequest request) {
        return ApiResponse.success(productSkuService.batchQuery(request.skuIds()));
    }
}