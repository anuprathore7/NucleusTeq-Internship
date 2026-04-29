package com.anup.restaurant_backend.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * OrderResponseDto
 *
 * Sent back to frontend after placing order
 * or when viewing order history.
 */
public class OrderResponseDto {

    private Long orderId;
    private Long restaurantId;
    private String restaurantName;
    private List<OrderItemResponseDto> items;
    private Double totalAmount;
    private String status;
    private LocalDateTime createdAt;

    public OrderResponseDto() {}

    public OrderResponseDto(Long orderId, Long restaurantId, String restaurantName,
                            List<OrderItemResponseDto> items, Double totalAmount,
                            String status, LocalDateTime createdAt) {
        this.orderId = orderId;
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
        this.items = items;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getOrderId() { return orderId; }
    public Long getRestaurantId() { return restaurantId; }
    public String getRestaurantName() { return restaurantName; }
    public List<OrderItemResponseDto> getItems() { return items; }
    public Double getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}