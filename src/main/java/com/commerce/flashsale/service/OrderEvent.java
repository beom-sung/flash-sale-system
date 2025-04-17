package com.commerce.flashsale.service;

import lombok.Builder;

@Builder
public record OrderEvent(
    String uuid,
    boolean success
) {

}
