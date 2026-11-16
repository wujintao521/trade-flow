package com.trade.request.order;

/**
 * 用户提交的单条购买明细。
 *
 * <p>只接收商品规格 ID 和购买数量，成交价格由服务端确定。
 * 输入合法性由后续业务服务统一校验。
 *
 * @param skuId    商品规格 ID
 * @param quantity 购买数量
 */
public record OrderItemRequest(
        Long skuId,
        Integer quantity
) {
}