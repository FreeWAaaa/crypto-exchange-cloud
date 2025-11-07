package com.cex.wallet.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.cex.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 充值记录实体
 * 
 * @author cex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wallet_deposit")
public class WalletDeposit extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 充值单号 */
    private String depositNo;

    /** 用户ID */
    private Long userId;

    /** 币种 */
    private String coin;

    /** 充值地址 */
    private String address;

    /** 充值数量 */
    private BigDecimal amount;

    /** 确认数量 */
    private BigDecimal confirmAmount;

    /** 当前确认次数 */
    private Integer confirmations;

    /** 需要确认次数 */
    private Integer needConfirmations;

    /** 交易哈希 */
    private String txHash;

    /** 充值状态（0待确认 1已确认 2失败） */
    private Integer status;

    /** 区块高度 */
    private Long blockHeight;

    /** 确认时间 */
    private java.util.Date confirmTime;

    /** 备注 */
    private String remark;
}
