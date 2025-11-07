package com.cex.activity.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.cex.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 签到活动实体
 * 
 * @author cex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sign_activity")
public class SignActivity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 赠送币种 */
    private String coin;

    /** 赠送数量 */
    private BigDecimal amount;

    /** 结束日期 */
    private Date endDate;

    /** 活动状态（0进行中 1已结束） */
    private Integer status;

    /** 活动标题 */
    private String title;

    /** 活动描述 */
    private String description;
}

