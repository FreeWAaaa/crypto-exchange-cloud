package com.cex.trade.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.cex.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 交易记录实体
 * 
 * @author cex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trade_record")
public class TradeRecord extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 交易ID */
    private String tradeId;

    /** 交易对 */
    private String symbol;

    /** 买方订单ID */
    private Long buyOrderId;

    /** 卖方订单ID */
    private Long sellOrderId;

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

    /** 手续费币种 */
    private String feeCoin;

    /** 交易时间 */
    private String tradeTime;

    /** 备注 */
    private String remark;
}
