package com.cex.wallet.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.cex.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 钱包流水记录实体
 * 
 * @author cex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wallet_transaction")
public class WalletTransaction extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 币种 */
    private String coin;

    /** 流水类型（1充值 2提现 3交易买入 4交易卖出 5手续费 6转账 7奖励 8系统调整） */
    private Integer flowType;

    /** 变动数量（正数表示增加，负数表示减少） */
    private BigDecimal amount;

    /** 变动前余额 */
    private BigDecimal balanceBefore;

    /** 变动后余额 */
    private BigDecimal balanceAfter;

    /** 手续费 */
    private BigDecimal fee;

    /** 关联ID（订单号、充值单号、提现单号等） */
    private String relatedId;

    /** 交易哈希 */
    private String txHash;

    /** 交易状态（0待确认 1已确认 2失败） */
    private Integer status;

    /** 确认数 */
    private Integer confirmations;

    /** 备注 */
    private String remark;
}
