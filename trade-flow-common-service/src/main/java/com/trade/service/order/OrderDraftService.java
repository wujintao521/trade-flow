package com.trade.service.order;

import com.trade.model.order.OrderDraft;
import com.trade.model.order.OrderSkuSnapshot;
import com.trade.request.order.OrderItemRequest;

import java.util.List;
import java.util.Map;

/**
 * 订单草稿业务接口。
 */
public interface OrderDraftService {

    /**
     * 根据购买明细和服务端商品信息构建订单草稿。
     *
     * @param buyerId 下单用户 ID，必须来自可信身份上下文
     * @param itemRequests 用户购买明细
     * @param skuSnapshots 服务端取得的商品信息，Key 为 SKU ID
     * @return 计算完成的订单草稿
     */
    OrderDraft buildDraft(Long buyerId, List<OrderItemRequest> itemRequests, Map<Long, OrderSkuSnapshot> skuSnapshots);
}