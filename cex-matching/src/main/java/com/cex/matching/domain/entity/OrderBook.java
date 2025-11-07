package com.cex.matching.domain.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单簿实体
 * 
 * @author cex
 */
@Data
public class OrderBook {

    /** 交易对 */
    private String symbol;

    /** 买卖方向（1买入 2卖出） */
    private Integer side;

    /** 价格 */
    private BigDecimal price;

    /** 数量 */
    private BigDecimal amount;

    /** 订单数量 */
    private Integer orderCount;

    /** 更新时间 */
    private Date updateTime;
}
