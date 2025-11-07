package com.cex.wallet.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.cex.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 钱包地址实体
 * 
 * @author cex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wallet_address")
public class WalletAddress extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 币种 */
    private String coin;

    /** 钱包地址 */
    private String address;

    /** Memo标签（EOS/XRP等币种需要） */
    private String memo;

    /** 私钥（加密存储，不建议存储） */
    private String privateKey;

    /** 地址类型（1充值地址 2提现地址 3冷钱包地址） */
    private Integer addressType;

    /** 地址来源（1RPC生成 2用户导入 3系统分配） */
    private Integer addressSource;

    /** 是否启用（0未启用 1已启用） */
    private Integer enabled;

    /** 使用次数 */
    private Integer useCount;

    /** 最后使用时间 */
    private java.util.Date lastUseTime;

    /** 备注 */
    private String remark;
}
