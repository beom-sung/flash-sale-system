package com.commerce.flashsale.service;

import com.commerce.flashsale.repository.Order;
import com.commerce.flashsale.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final ValidationService validationService;
    private final ProductRedis productRedis;
    private final OrderRepository orderRepository;

    public boolean create(String uuid) {
        boolean validate = validationService.validate(uuid);
        productRedis.check();

        orderRepository.save(Order.builder().uuid(uuid).success(validate).build());
        return validate;
    }
}
