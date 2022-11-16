package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.model.OrderRequest;
import com.ecommerce.orderservice.model.OrderResponse;

public interface OrderService {
    long placeOrder(OrderRequest orderRequest);

    OrderResponse getOrderDetails(long orderId);
}
