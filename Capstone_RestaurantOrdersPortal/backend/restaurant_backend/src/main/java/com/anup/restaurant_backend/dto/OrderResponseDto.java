package com.anup.restaurant_backend.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO returned to the client after placing or fetching an order.
 * Includes the delivery address selected at checkout.
 */
public class OrderResponseDto {

    private Long orderId;
    private Long restaurantId;
    private String restaurantName;
    private List<OrderItemResponseDto> items;
    private Double totalAmount;
    private String status;
    private LocalDateTime createdAt;
    private String deliveryAddress;   // full address string shown to user

    public OrderResponseDto() {}

    public OrderResponseDto(Long orderId, Long restaurantId, String restaurantName,
                            List<OrderItemResponseDto> items, Double totalAmount,
                            String status, LocalDateTime createdAt,
                            String deliveryAddress) {
        this.orderId         = orderId;
        this.restaurantId    = restaurantId;
        this.restaurantName  = restaurantName;
        this.items           = items;
        this.totalAmount     = totalAmount;
        this.status          = status;
        this.createdAt       = createdAt;
        this.deliveryAddress = deliveryAddress;
    }

    public Long                    getOrderId()        { return orderId; }
    public Long                    getRestaurantId()   { return restaurantId; }
    public String                  getRestaurantName() { return restaurantName; }
    public List<OrderItemResponseDto> getItems()       { return items; }
    public Double                  getTotalAmount()    { return totalAmount; }
    public String                  getStatus()         { return status; }
    public LocalDateTime           getCreatedAt()      { return createdAt; }
    public String                  getDeliveryAddress(){ return deliveryAddress; }
}