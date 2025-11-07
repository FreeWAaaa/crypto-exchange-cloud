package com.cex.activity.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.cex.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 签到记录实体
 * 
 * @author cex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sign_record")
public class SignRecord extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 活动ID */
    private Long activityId;

    /** 赠送币种 */
    private String coin;

    /** 赠送数量 */
    private BigDecimal amount;

    /** 签到日期 */
    private Date signDate;

    /** 连续签到天数 */
    private Integer consecutiveDays;

    /** 备注 */
    private String remark;
}

