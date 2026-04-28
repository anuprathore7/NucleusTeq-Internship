package com.anup.restaurant_backend.enums;

/**
 * ============================================
 *   OrderStatus Enum
 * ============================================
 *
 *  ORDER LIFECYCLE (from SRS FR-11):
 *
 * Customer places order → PLACED
 * Restaurant sees it    → PENDING
 * Food is on the way    → OUT_FOR_DELIVERY
 * Customer received     → COMPLETED
 * Someone cancelled     → CANCELLED
 *
 */
public enum OrderStatus {
    PLACED,
    PENDING,
    DELIVERED,
    COMPLETED,
    CANCELLED
}