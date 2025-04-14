package com.commerce.flashsale.service;

import com.commerce.flashsale.repository.Order;
import com.commerce.flashsale.repository.OrderRepository;
import com.commerce.flashsale.repository.RedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final ValidationService validationService;
    private final RedisRepository redisRepository;
    private final OrderRepository orderRepository;

    public boolean create(String uuid) {
        boolean validate = validationService.validate(uuid);
        redisRepository.save(uuid, validate);
        orderRepository.save(Order.builder().uuid(uuid).success(validate).build());
        return validate;
    }
}
