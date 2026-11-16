package com.trade.service.order.impl;

import com.trade.common.exception.BusinessException;
import com.trade.enums.order.OrderErrorCode;
import com.trade.enums.order.OrderStatus;
import com.trade.model.order.OrderDraft;
import com.trade.model.order.OrderItemDraft;
import com.trade.model.order.OrderSkuSnapshot;
import com.trade.request.order.OrderItemRequest;
import com.trade.service.order.OrderItemNormalizer;

import com.trade.service.order.OrderDraftService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 订单草稿业务实现。
 *
 * <p>只进行校验、金额计算和快照组装，不写数据库、不预占库存。
 */
@Service
public class OrderDraftServiceImpl implements OrderDraftService {

    /**
     * 当前金额上限，与计划中的 DECIMAL(12, 2) 存储范围对应。
     */
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("9999999999.99");

    private final OrderItemNormalizer orderItemNormalizer;

    public OrderDraftServiceImpl(OrderItemNormalizer orderItemNormalizer) {
        this.orderItemNormalizer = Objects.requireNonNull(orderItemNormalizer, "orderItemNormalizer");
    }

    @Override
    public OrderDraft buildDraft(Long buyerId, List<OrderItemRequest> itemRequests, Map<Long, OrderSkuSnapshot> skuSnapshots) {

        if (buyerId == null || buyerId <= 0) {
            throw businessError(OrderErrorCode.INVALID_ORDER_REQUEST, "下单用户 ID 必须为正整数");
        }

        List<OrderItemRequest> normalizedItems = orderItemNormalizer.normalize(itemRequests);

        if (skuSnapshots == null) {
            throw businessError(OrderErrorCode.ORDER_PRICING_DATA_INVALID, "服务端商品信息不能为空");
        }

        List<OrderItemDraft> draftItems = new ArrayList<>(normalizedItems.size());
        BigDecimal totalAmount = new BigDecimal("0.00");

        for (OrderItemRequest itemRequest : normalizedItems) {

            Long skuId = itemRequest.skuId();
            OrderSkuSnapshot skuSnapshot = skuSnapshots.get(skuId);

            if (skuSnapshot == null) {
                throw businessError(OrderErrorCode.ORDER_SKU_UNAVAILABLE, "商品不存在，SKU ID：" + skuId);
            }

            validateSkuSnapshot(skuId, skuSnapshot);

            if (!skuSnapshot.saleable()) {
                throw businessError(OrderErrorCode.ORDER_SKU_UNAVAILABLE, "商品不可售，SKU ID：" + skuId);
            }

            BigDecimal unitPrice = normalizePrice(skuSnapshot.unitPrice());
            int quantity = itemRequest.quantity();

            BigDecimal lineAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));
            totalAmount = totalAmount.add(lineAmount);

            if (totalAmount.compareTo(MAX_AMOUNT) > 0) {
                throw businessError(OrderErrorCode.ORDER_AMOUNT_EXCEEDED, "订单总金额超出允许范围");
            }

            OrderItemDraft draftItem = new OrderItemDraft(skuId, skuSnapshot.skuCode(), skuSnapshot.skuName(), unitPrice, quantity, lineAmount);
            draftItems.add(draftItem);
        }

        return new OrderDraft(buyerId, totalAmount, "CNY", OrderStatus.PENDING_PAYMENT, draftItems);
    }

    private void validateSkuSnapshot(Long expectedSkuId, OrderSkuSnapshot skuSnapshot) {

        if (!expectedSkuId.equals(skuSnapshot.skuId())) {
            throw businessError(OrderErrorCode.ORDER_PRICING_DATA_INVALID, "商品信息与请求 SKU 不一致");
        }

        if (skuSnapshot.skuCode() == null || skuSnapshot.skuCode().isBlank()) {
            throw businessError(OrderErrorCode.ORDER_PRICING_DATA_INVALID, "商品编码不能为空");
        }

        if (skuSnapshot.skuName() == null || skuSnapshot.skuName().isBlank()) {
            throw businessError(OrderErrorCode.ORDER_PRICING_DATA_INVALID, "商品名称不能为空");
        }
    }

    private BigDecimal normalizePrice(BigDecimal unitPrice) {

        if (unitPrice == null || unitPrice.signum() < 0 || unitPrice.compareTo(MAX_AMOUNT) > 0) {
            throw businessError(OrderErrorCode.ORDER_PRICING_DATA_INVALID, "商品价格超出允许范围");
        }

        try {
            // 人民币精确到分，不静默舍入存在非零厘位的异常价格。
            return unitPrice.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw businessError(OrderErrorCode.ORDER_PRICING_DATA_INVALID, "商品价格最多支持两位小数");
        }
    }

    private BusinessException businessError(OrderErrorCode errorCode, String message) {
        return new BusinessException(errorCode.getCode(), message);
    }
}