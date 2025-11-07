package com.cex.matching.domain.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 成交记录实体
 * 
 * @author cex
 */
@Data
public class TradeRecord {

    /** 成交ID */
    private String tradeId;

    /** 交易对 */
    private String symbol;

    /** 买方订单号 */
    private String buyOrderNo;

    /** 卖方订单号 */
    private String sellOrderNo;

    /** 买方用户ID */
    private Long buyUserId;

    /** 卖方用户ID */
    private Long sellUserId;

    /** 成交价格 */
    private BigDecimal price;

    /** 成交数量 */
    private BigDecimal amount;

    /** 成交金额 */
    private BigDecimal money;

    /** 买方手续费 */
    private BigDecimal buyFee;

    /** 卖方手续费 */
    private BigDecimal sellFee;

    /** 成交时间 */
    private Date tradeTime;
}
