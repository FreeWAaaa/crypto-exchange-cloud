package com.cex.common.enums;

/**
 * 订单状态枚举
 *
 * @author cex
 */
public enum OrderStatus {
    /** 交易中 */
    TRADING,
    /** 已完成 */
    COMPLETED,
    /** 已取消 */
    CANCELED,
    /** 超时 */
    OVERTIMED;
}

