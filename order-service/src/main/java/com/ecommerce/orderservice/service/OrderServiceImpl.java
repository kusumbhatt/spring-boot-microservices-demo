package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.exception.CustomException;
import com.ecommerce.orderservice.external.client.PaymentService;
import com.ecommerce.orderservice.external.client.ProductService;
import com.ecommerce.orderservice.external.request.PaymentRequest;
import com.ecommerce.orderservice.external.response.PaymentResponse;
import com.ecommerce.orderservice.external.response.ProductResponse;
import com.ecommerce.orderservice.model.OrderRequest;
import com.ecommerce.orderservice.model.OrderResponse;
import com.ecommerce.orderservice.repository.OrderRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Service
@Log4j2
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private PaymentService paymentService;
    @Override
    public long placeOrder(OrderRequest orderRequest) {
        // Order Entity - Save the data with status order created
        // Block products - reduce the quantity
        // payment service - payment - success - complete else cancelled
        log.info("Placing order request: {}", orderRequest);

        productService.reduceQuantity(orderRequest.getProductId(), orderRequest.getQuantity());

        log.info("Creating order with status CREATED");
        Order order = Order.builder()
                .amount(orderRequest.getTotalAmount())
                .orderStatus("CREATED")
                .orderDate(Instant.now())
                .productId(orderRequest.getProductId())
                .quantity(orderRequest.getQuantity())
                .build();
        order = orderRepository.save(order);

        log.info("Calling payment service to complete the payment");

        PaymentRequest paymentRequest = PaymentRequest.builder()
                .orderId(order.getId())
                .paymentMode(orderRequest.getPaymentMode())
                .amount(orderRequest.getTotalAmount())
                .build();
        String orderStatus = null;
        try {
            paymentService.doPayment(paymentRequest);
            log.info("Payment done successfully. Changing order status to PLACED");
            orderStatus = "PLACED";
        } catch(Exception e) {
            log.error("Error occurred in payment. Changing order status to PAYMENT_FAILED ");
            orderStatus = "PAYMENT_FAILED";
        }

        order.setOrderStatus(orderStatus);
        orderRepository.save(order);
        log.info("order places successfully with order id: {}", order.getId());
        return order.getId();
    }

    @Override
    public OrderResponse getOrderDetails(long orderId) {
        log.info("Get order details for orderId: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(()->new CustomException(
                        "Order not found for orderId " + orderId,
                        "NOT_FOUND",
                        404)
                );

        log.info("Invoking Product service to fetch the product for id: {}", order.getProductId());

        ProductResponse productResponse = restTemplate.getForObject(
                "http://PRODUCT-SERVICE/product/" + order.getProductId(),
                ProductResponse.class
        );

        OrderResponse.ProductDetails productDetails = OrderResponse.ProductDetails.builder()
                .productId(productResponse.getProductId())
                .price(productResponse.getPrice())
                .productName(productResponse.getProductName())
                .quantity(productResponse.getQuantity())
                .build();

        log.info("Getting payment details from the payment service");

        PaymentResponse paymentResponse = restTemplate.getForObject(
                "http://PAYMENT-SERVICE/payment/order/" + order.getId(),
                PaymentResponse.class
        );

        OrderResponse.PaymentDetails paymentDetails = OrderResponse.PaymentDetails.builder()
                .paymentId(paymentResponse.getPaymentId())
                .paymentStatus(paymentResponse.getStatus())
                .paymentMode(paymentResponse.getPaymentMode())
                .paymentDate(paymentResponse.getPaymentDate())
                .build();

        OrderResponse orderResponse = OrderResponse.builder()
                .orderId(order.getId())
                .orderStatus(order.getOrderStatus())
                .orderDate(order.getOrderDate())
                .amount(order.getAmount())
                .productDetails(productDetails)
                .paymentDetails(paymentDetails)
                .build();


        return orderResponse;
    }
}
