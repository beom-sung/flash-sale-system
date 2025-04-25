package com.commerce.flashsale.controller;

public record ProductRequest(
    String productName,
    int stockCount
) {

}
