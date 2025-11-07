package com.cex.activity.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.cex.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 红包实体
 * 
 * @author cex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("red_envelope")
public class RedEnvelope extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 红包编号 */
    private String envelopeNo;

    /** 发起人ID（0表示平台红包） */
    private Long userId;

    /** 红包类型（0随机 1定额） */
    private Integer envelopeType;

    /** 红包总额 */
    private BigDecimal totalAmount;

    /** 已领取金额 */
    private BigDecimal receivedAmount;

    /** 红包数量 */
    private Integer count;

    /** 已领取数量 */
    private Integer receivedCount;

    /** 红包币种 */
    private String coin;

    /** 红包标题 */
    private String title;

    /** 红包描述 */
    private String description;

    /** 状态（0领取中 1已领完 2已过期） */
    private Integer status;

    /** 过期小时数 */
    private Integer expiredHours;

    /** 是否邀请红包（0否 1是） */
    private Integer isInvite;

    /** 是否平台红包（0否 1是） */
    private Integer isPlatform;

    /** 最大随机金额 */
    private BigDecimal maxRandom;
}

