package com.cex.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.cex.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户邀请关系实体
 * 
 * @author cex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_invite_relation")
public class UserInviteRelation extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 邀请人ID */
    private Long inviterId;

    /** 邀请层级（1一级 2二级 3三级） */
    private Integer inviteLevel;

    /** 奖励状态（0未发放 1已发放） */
    private Integer rewardStatus;

    /** 奖励金额 */
    private java.math.BigDecimal rewardAmount;

    /** 奖励币种 */
    private String rewardCoin;

    /** 奖励发放时间 */
    private java.util.Date rewardTime;

    /** 备注 */
    private String remark;
}

