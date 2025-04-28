package com.commerce.flashsale.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class ValidationService {

    public boolean validate() {
        int random = ThreadLocalRandom.current().nextInt(10);
        return random != 0;
    }
}
