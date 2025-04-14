package com.commerce.flashsale.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class ValidationService {

    public boolean validate(String uuid) {
        int random = ThreadLocalRandom.current().nextInt(10);
        return random != 0;
    }
}
