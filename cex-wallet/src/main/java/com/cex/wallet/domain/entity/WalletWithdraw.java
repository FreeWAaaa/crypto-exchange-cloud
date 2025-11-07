package com.cex.wallet.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.cex.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 提现记录实体
 * 
 * @author cex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wallet_withdraw")
public class WalletWithdraw extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 提现单号 */
    private String withdrawNo;

    /** 用户ID */
    private Long userId;

    /** 币种 */
    private String coin;

    /** 提现地址 */
    private String address;

    /** 提现数量 */
    private BigDecimal amount;

    /** 手续费 */
    private BigDecimal fee;

    /** 实际到账数量 */
    private BigDecimal actualAmount;

    /** 交易哈希 */
    private String txHash;

    /** 提现状态（0待审核 1审核通过 2已发送 3已完成 4已拒绝 5已撤销） */
    private Integer status;

    /** 审核时间 */
    private java.util.Date auditTime;

    /** 审核人ID */
    private Long auditorId;

    /** 审核人 */
    private String auditor;

    /** 审核备注 */
    private String auditRemark;

    /** 发送时间 */
    private java.util.Date sendTime;

    /** 完成时间 */
    private java.util.Date completeTime;

    /** 区块高度 */
    private Long blockHeight;

    /** 确认次数 */
    private Integer confirmations;

    /** 备注 */
    private String remark;
}
