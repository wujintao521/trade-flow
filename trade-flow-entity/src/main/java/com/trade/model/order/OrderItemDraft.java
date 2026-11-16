package com.trade.model.order;

import java.math.BigDecimal;

/**
 * 服务端生成的订单明细草稿。
 *
 * <p>保存本次计算时的商品信息和成交价格。
 * 本对象尚未持久化，也不表示库存已经预占成功。
 *
 * @param skuId           商品规格 ID
 * @param skuCodeSnapshot 商品规格编码快照
 * @param skuNameSnapshot 商品名称及规格快照
 * @param unitPrice       成交单价
 * @param quantity        合并并校验后的购买数量
 * @param lineAmount      明细金额：成交单价乘以购买数量
 */
public record OrderItemDraft(
        Long skuId,
        String skuCodeSnapshot,
        String skuNameSnapshot,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineAmount
) {
}