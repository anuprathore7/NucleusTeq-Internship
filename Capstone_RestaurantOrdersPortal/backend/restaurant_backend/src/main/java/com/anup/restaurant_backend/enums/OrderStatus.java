package com.anup.restaurant_backend.enums;

/**
 * 📌 OrderStatus ENUM
 *
 * 🧠 Real-life meaning:
 * Defines different stages of an order lifecycle
 *
 * Why ENUM?
 * → prevents invalid values
 * → makes code clean
 * → industry standard practice
 */
public enum OrderStatus {

    /**
     *  Order just placed
     */
    PLACED,

    /**
     *  Restaurant is preparing food
     */
    PREPARING,

    /**
     *  Out for delivery
     */
    OUT_FOR_DELIVERY,

    /**
     *  Order completed
     */
    DELIVERED,

    /**
     *  Order cancelled
     */
    CANCELLED
}