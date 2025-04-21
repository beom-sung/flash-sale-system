package com.commerce.flashsale.message;

import lombok.Builder;

@Builder
public record OrderEvent(
    String uuid,
    boolean success
) {

}
