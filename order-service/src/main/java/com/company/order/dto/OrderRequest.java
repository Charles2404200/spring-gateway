package com.company.order.dto;

import java.time.LocalDateTime;

public class OrderRequest {
    private String orderDetails;

    public OrderRequest() {
    }

    public OrderRequest(String orderDetails) {
        this.orderDetails = orderDetails;
    }

    public String getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(String orderDetails) {
        this.orderDetails = orderDetails;
    }
}

