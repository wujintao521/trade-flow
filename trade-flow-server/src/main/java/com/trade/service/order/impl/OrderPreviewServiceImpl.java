package com.trade.service.order.impl;

import com.trade.common.exception.BusinessException;
import com.trade.dao.product.ProductSkuMapper;
import com.trade.entity.product.ProductSku;
import com.trade.enums.order.OrderErrorCode;
import com.trade.enums.product.SkuSaleStatus;
import com.trade.model.order.OrderDraft;
import com.trade.model.order.OrderItemDraft;
import com.trade.model.order.OrderSkuSnapshot;
import com.trade.model.security.CurrentUser;
import com.trade.request.order.OrderItemRequest;
import com.trade.request.order.OrderPreviewRequest;
import com.trade.response.order.OrderPreviewItemResponse;
import com.trade.response.order.OrderPreviewResponse;
import com.trade.service.order.OrderDraftService;
import com.trade.service.order.OrderItemNormalizer;
import com.trade.service.order.OrderPreviewService;
import com.trade.service.security.CurrentUserService;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 订单预览只读商品并计算金额，不写订单、不预占库存。
 */
@Service
public class OrderPreviewServiceImpl implements OrderPreviewService {

    private final CurrentUserService currentUserService;

    private final OrderItemNormalizer orderItemNormalizer;

    private final ProductSkuMapper productSkuMapper;

    private final OrderDraftService orderDraftService;

    public OrderPreviewServiceImpl(CurrentUserService currentUserService, OrderItemNormalizer orderItemNormalizer, ProductSkuMapper productSkuMapper, OrderDraftService orderDraftService) {
        this.currentUserService = Objects.requireNonNull(currentUserService, "currentUserService");
        this.orderItemNormalizer = Objects.requireNonNull(orderItemNormalizer, "orderItemNormalizer");
        this.productSkuMapper = Objects.requireNonNull(productSkuMapper, "productSkuMapper");
        this.orderDraftService = Objects.requireNonNull(orderDraftService, "orderDraftService");
    }

    @Override
    public OrderPreviewResponse preview(OrderPreviewRequest request) {

        CurrentUser currentUser = currentUserService.getRequiredUser();
        Long buyerId = currentUser.userId();

        if (request == null) {
            throw new BusinessException(OrderErrorCode.INVALID_ORDER_REQUEST.getCode(), "订单预览请求不能为空");
        }

        // 查询商品之前，先检查数量、明细规模，并合并重复 SKU。
        List<OrderItemRequest> normalizedItems = orderItemNormalizer.normalize(request.items());

        List<Long> skuIds = new ArrayList<>(normalizedItems.size());

        for (OrderItemRequest item : normalizedItems) {
            skuIds.add(item.skuId());
        }

        // 商品价格和名称来自数据库。
        List<ProductSku> productSkus = productSkuMapper.selectOnSaleByIds(skuIds);

        Map<Long, OrderSkuSnapshot> skuSnapshots = new HashMap<>(productSkus.size());

        for (ProductSku sku : productSkus) {
            OrderSkuSnapshot snapshot = new OrderSkuSnapshot(sku.id(), sku.skuCode(), sku.skuName(), sku.salePrice(), sku.saleStatus() == SkuSaleStatus.ON_SALE);
            skuSnapshots.put(sku.id(), snapshot);
        }

        OrderDraft draft = orderDraftService.buildDraft(buyerId, normalizedItems, skuSnapshots);

        List<OrderPreviewItemResponse> items = new ArrayList<>(draft.items().size());

        for (OrderItemDraft item : draft.items()) {
            OrderPreviewItemResponse responseItem = new OrderPreviewItemResponse(item.skuId(), item.skuCodeSnapshot(), item.skuNameSnapshot(), item.unitPrice(), item.quantity(), item.lineAmount());
            items.add(responseItem);
        }

        return new OrderPreviewResponse(draft.buyerId(), draft.totalAmount(), draft.currency(), items);
    }
}