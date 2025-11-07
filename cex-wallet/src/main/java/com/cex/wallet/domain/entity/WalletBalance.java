package com.cex.wallet.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.cex.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 用户资产实体
 * 
 * @author cex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wallet_balance")
public class WalletBalance extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 币种 */
    private String coin;

    /** 可用余额 */
    private BigDecimal availableBalance;

    /** 冻结余额 */
    private BigDecimal frozenBalance;

    /** 总余额（可用+冻结，可计算得出） */
    private BigDecimal totalBalance;

    /** 充值地址 */
    private String address;

    /** Memo标签（EOS/XRP等需要） */
    private String memo;

    /** 待释放余额（用于活动奖励分期释放） */
    private BigDecimal toReleased;

    /** 钱包锁定状态（0正常 1锁定） */
    private Integer isLock;

    /** 版本号（乐观锁，用于并发控制） */
    @Version
    private Integer version;

    /**
     * 计算总余额
     */
    public void calculateTotalBalance() {
        BigDecimal available = this.availableBalance != null ? this.availableBalance : BigDecimal.ZERO;
        BigDecimal frozen = this.frozenBalance != null ? this.frozenBalance : BigDecimal.ZERO;
        this.totalBalance = available.add(frozen);
    }
}
