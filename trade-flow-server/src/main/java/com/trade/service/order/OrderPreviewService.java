package com.trade.service.order;

import com.trade.request.order.OrderPreviewRequest;
import com.trade.response.order.OrderPreviewResponse;

/**
 * 查询当前商品价格，生成当前登录用户的订单预览。
 */
public interface OrderPreviewService {

    OrderPreviewResponse preview(OrderPreviewRequest request);
}