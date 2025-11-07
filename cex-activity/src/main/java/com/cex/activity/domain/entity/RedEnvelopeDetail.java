package com.cex.activity.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.cex.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 红包领取详情
 * 
 * @author cex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("red_envelope_detail")
public class RedEnvelopeDetail extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 红包ID */
    private Long envelopeId;

    /** 红包编号 */
    private String envelopeNo;

    /** 领取人ID */
    private Long userId;

    /** 领取金额 */
    private BigDecimal amount;

    /** 领取币种 */
    private String coin;

    /** 备注 */
    private String remark;
}

