package com.cex.activity.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.cex.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 活动实体
 * 
 * @author cex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("activity")
public class Activity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 活动名称 */
    private String name;

    /** 活动类型（1抢购 2分配 3挖矿 4红包） */
    private Integer type;

    /** 活动状态（0未开始 1进行中 2已结束） */
    private Integer status;

    /** 开始时间 */
    private String startTime;

    /** 结束时间 */
    private String endTime;

    /** 活动币种 */
    private String coin;

    /** 活动总数量 */
    private BigDecimal totalAmount;

    /** 已发放数量 */
    private BigDecimal releasedAmount;

    /** 活动价格 */
    private BigDecimal price;

    /** 参与人数限制 */
    private Integer participantLimit;

    /** 已参与人数 */
    private Integer participantCount;

    /** 活动描述 */
    private String description;

    /** 活动规则 */
    private String rules;

    /** 是否启用（0未启用 1已启用） */
    private Integer enabled;
}
