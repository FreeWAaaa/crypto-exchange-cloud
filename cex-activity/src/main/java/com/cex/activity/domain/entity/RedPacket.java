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
@TableName("red_packet")
public class RedPacket extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 红包ID */
    private String packetId;

    /** 发送者ID */
    private Long senderId;

    /** 红包类型（1普通红包 2拼手气红包） */
    private Integer type;

    /** 红包币种 */
    private String coin;

    /** 红包总金额 */
    private BigDecimal totalAmount;

    /** 红包总个数 */
    private Integer totalCount;

    /** 已领取个数 */
    private Integer receivedCount;

    /** 已领取金额 */
    private BigDecimal receivedAmount;

    /** 红包状态（0未发送 1已发送 2已领完 3已过期） */
    private Integer status;

    /** 过期时间 */
    private String expireTime;

    /** 红包祝福语 */
    private String blessing;

    /** 是否公开（0不公开 1公开） */
    private Integer isPublic;
}
