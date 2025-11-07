package com.cex.common.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单DTO（供模块间通信）
 *
 * @author cex
 */
@Data
public class OrderDTO {

    /** 订单号 */
    private String orderNo;

    /** 用户ID */
    private Long userId;

    /** 交易对 */
    private String symbol;

    /** 订单类型（1限价单 2市价单） */
    private Integer orderType;

    /** 买卖方向（1买入 2卖出） */
    private Integer side;

    /** 订单价格 */
    private BigDecimal price;

    /** 订单数量 */
    private BigDecimal amount;

    /** 已成交数量 */
    private BigDecimal filledAmount = BigDecimal.ZERO;

    /** 已成交金额 */
    private BigDecimal filledMoney = BigDecimal.ZERO;

    /** 手续费率 */
    private BigDecimal feeRate;

    /** 手续费币种 */
    private String feeCoin;

    /** 订单状态（0待成交 1部分成交 2完全成交 3已撤销） */
    private Integer status = 0;

    /** 创建时间 */
    private Long createTime;
}

