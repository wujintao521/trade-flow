package com.trade.service.order;

import com.trade.common.exception.BusinessException;
import com.trade.enums.order.OrderErrorCode;
import com.trade.request.order.OrderItemRequest;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 校验购买明细，合并重复 SKU，并固定输出顺序。
 */
@Component
public class OrderItemNormalizer {

    private static final int MAX_INPUT_ITEMS = 100;

    private static final int MAX_DISTINCT_SKUS = 50;

    private static final int MAX_QUANTITY_PER_SKU = 9999;

    public List<OrderItemRequest> normalize(List<OrderItemRequest> itemRequests) {

        if (itemRequests == null || itemRequests.isEmpty()) {
            throw invalidRequest("购买明细不能为空");
        }

        if (itemRequests.size() > MAX_INPUT_ITEMS) {
            throw invalidRequest("原始购买明细不能超过 100 条");
        }

        // Key：SKU ID；Value：该 SKU 累计购买数量。
        // TreeMap 保证按 SKU ID 升序遍历。
        Map<Long, Integer> quantityBySku = new TreeMap<>();

        for (OrderItemRequest itemRequest : itemRequests) {

            if (itemRequest == null) {
                throw invalidRequest("购买明细中不能包含空元素");
            }

            Long skuId = itemRequest.skuId();
            Integer quantity = itemRequest.quantity();

            if (skuId == null || skuId <= 0) {
                throw invalidRequest("SKU ID 必须为正整数");
            }

            if (quantity == null || quantity <= 0 || quantity > MAX_QUANTITY_PER_SKU) {

                throw invalidRequest("每个 SKU 的购买数量必须为 1 到 9999");
            }

            int previousQuantity = quantityBySku.getOrDefault(skuId, 0);
            // 在相加前检查合并上限。
            if (quantity > MAX_QUANTITY_PER_SKU - previousQuantity) {
                throw invalidRequest("同一 SKU 合并后的数量不能超过 9999");
            }
            quantityBySku.put(skuId, previousQuantity + quantity);
        }

        if (quantityBySku.size() > MAX_DISTINCT_SKUS) {
            throw invalidRequest("一张订单最多购买 50 种 SKU");
        }

        List<OrderItemRequest> normalizedItems = new ArrayList<>(quantityBySku.size());

        for (Map.Entry<Long, Integer> entry : quantityBySku.entrySet()) {
            normalizedItems.add(new OrderItemRequest(entry.getKey(), entry.getValue()));
        }

        return List.copyOf(normalizedItems);
    }

    private BusinessException invalidRequest(String message) {
        return new BusinessException(OrderErrorCode.INVALID_ORDER_REQUEST.getCode(), message);
    }
}