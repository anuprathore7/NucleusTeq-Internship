package com.anup.restaurant_backend.entity;

import com.anup.restaurant_backend.enums.OrderStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity representing a customer order stored in the "orders" table.
 */
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Customer who placed the order. */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    /** Restaurant the order was placed at. */
    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    /** Snapshot of all items included in this order. */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems;

    /** Total amount charged for this order. */
    private Double totalAmount;

    /** Current status in the order lifecycle. */
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    /** Timestamp when the order was placed. Used for the 30-second cancellation rule. */
    private LocalDateTime createdAt;

    /** Delivery address selected by the customer at checkout. */
    @ManyToOne
    @JoinColumn(name = "delivery_address_id")
    private Address deliveryAddress;

    public Order() {}

    public Long getId()                          { return id; }
    public void setId(Long id)                   { this.id = id; }

    public UserEntity getUser()                  { return user; }
    public void setUser(UserEntity user)         { this.user = user; }

    public Restaurant getRestaurant()            { return restaurant; }
    public void setRestaurant(Restaurant r)      { this.restaurant = r; }

    public List<OrderItem> getOrderItems()       { return orderItems; }
    public void setOrderItems(List<OrderItem> i) { this.orderItems = i; }

    public Double getTotalAmount()               { return totalAmount; }
    public void setTotalAmount(Double t)         { this.totalAmount = t; }

    public OrderStatus getStatus()               { return status; }
    public void setStatus(OrderStatus status)    { this.status = status; }

    public LocalDateTime getCreatedAt()          { return createdAt; }
    public void setCreatedAt(LocalDateTime t)    { this.createdAt = t; }

    public Address getDeliveryAddress()          { return deliveryAddress; }
    public void setDeliveryAddress(Address a)    { this.deliveryAddress = a; }
}