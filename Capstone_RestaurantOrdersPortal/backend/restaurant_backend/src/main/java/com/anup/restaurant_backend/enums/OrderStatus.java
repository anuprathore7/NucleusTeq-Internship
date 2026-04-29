


package com.anup.restaurant_backend.enums;

/**
 * ============================================
 *   OrderStatus Enum  —  UPDATED
 * ============================================
 *
 *
 *
 *  Customer places order  →  PLACED
 *  Owner sees it          →  PENDING       (owner sets this)
 *  Owner accepts          →  ACCEPTED      (owner sets this)
 *  Food is on the way     →  OUT_FOR_DELIVERY (owner sets this)
 *  Customer received      →  COMPLETED     (owner sets this)
 *  Someone cancelled      →  CANCELLED
 *
 */
public enum OrderStatus {
    PLACED,
    PENDING,
    ACCEPTED,
    OUT_FOR_DELIVERY,
    COMPLETED,
    CANCELLED
}