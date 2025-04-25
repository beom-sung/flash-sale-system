package com.commerce.flashsale.controller;

import com.commerce.flashsale.service.OrderHistoryService;
import com.commerce.flashsale.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderHistoryService orderHistoryService;

    @PostMapping("/orders")
    public OrderResponse createOrder(@RequestBody OrderRequest request) {
        boolean success = orderService.create(request.uuid(), request.productName());
        return new OrderResponse(request.uuid(), success);
    }

    @GetMapping("/orders")
    public Integer getOrderSuccessCount(@RequestParam(name = "productName") String productName) {
        return orderHistoryService.getOrderSuccessCount(productName);
    }

    @PostMapping("/products")
    public void createProduct(@RequestBody ProductRequest request) {
        orderService.createProduct(request.productName(), request.stockCount());
    }
}
