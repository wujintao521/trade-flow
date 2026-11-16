package com.trade.dao.product;

import com.trade.entity.product.ProductSku;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品SKU数据访问。
 */
public interface ProductSkuMapper {

    ProductSku selectOnSaleById(@Param("skuId") Long skuId);

    List<ProductSku> selectOnSaleByIds(@Param("skuIds") List<Long> skuIds);
}