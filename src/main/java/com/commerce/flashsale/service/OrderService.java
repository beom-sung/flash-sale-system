package com.commerce.flashsale.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final ValidationService validationService;
    private final ProductRedis productRedis;

    public boolean create(String uuid) {
        boolean validate = validationService.validate(uuid);
        productRedis.check();
        return validate;
    }
}
