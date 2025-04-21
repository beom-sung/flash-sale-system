package com.commerce.flashsale.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class ValidationService {

    private final OrderHistoryService orderHistoryService;

    public boolean validate(String uuid) {
        boolean success = orderHistoryService.hasSuccessHistory(uuid);
        if(success) {
            return false;
        }
        int random = ThreadLocalRandom.current().nextInt(10);
        return random != 0;
    }
}
