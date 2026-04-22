package com.anup.restaurant_backend.entity;

import com.anup.restaurant_backend.enums.OrderStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 *  Order Entity
 *
 *  Real-life meaning:
 * When user clicks "Place Order"
 * → Cart becomes Order
 * → Payment happens
 * → Order gets tracked
 */
@Entity
@Table(name = "orders")
public class Order {

    /**
     *  Primary Key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     *  Order belongs to ONE user
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    /**
     *  Order belongs to ONE restaurant
     */
    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    /**
     *  Total amount of order
     */
    private Double totalAmount;

    /**
     *  Order status
     * Example: PLACED, PREPARING, DELIVERED
     */
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    /**
     *  Order time
     */
    private LocalDateTime orderTime;

    /**
     *  ONE order has MANY order items
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items;

    public Order() {
    }

    // ================= GETTERS & SETTERS =================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserEntity getUser() {
        return user;
    }

    /**
     *  Links order to user → user_id stored
     */
    public void setUser(UserEntity user) {
        this.user = user;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    /**
     *  Links order to restaurant → restaurant_id stored
     */
    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    /**
     *  Calculated from cart
     */
    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    /**
     *  Controls order lifecycle
     */
    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getOrderTime() {
        return orderTime;
    }

    /**
     *  Set when order is placed
     */
    public void setOrderTime(LocalDateTime orderTime) {
        this.orderTime = orderTime;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    /**
     *  Holds all items in order
     */
    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
}