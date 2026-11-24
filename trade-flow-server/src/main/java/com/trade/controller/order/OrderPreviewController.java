package com.trade.controller.order;

import com.trade.common.response.ApiResponse;
import com.trade.request.order.OrderPreviewRequest;
import com.trade.response.order.OrderPreviewResponse;
import com.trade.service.order.OrderPreviewService;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * 当前登录用户的订单确认预览接口。
 */
@RestController
@RequestMapping("/api/orders")
public class OrderPreviewController {

    private final OrderPreviewService orderPreviewService;

    public OrderPreviewController(OrderPreviewService orderPreviewService) {
        this.orderPreviewService = Objects.requireNonNull(orderPreviewService, "orderPreviewService");
    }

    @PostMapping("/preview")
    public ResponseEntity<ApiResponse<OrderPreviewResponse>> preview(@RequestBody OrderPreviewRequest request) {
        OrderPreviewResponse response = orderPreviewService.preview(request);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).header(HttpHeaders.PRAGMA, "no-cache").body(ApiResponse.success(response));
    }
}