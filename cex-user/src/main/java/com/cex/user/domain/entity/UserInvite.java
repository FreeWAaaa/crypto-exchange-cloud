package com.cex.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.cex.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户邀请记录实体
 * 
 * @author cex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_invite")
public class UserInvite extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 邀请人ID */
    private Long inviterId;

    /** 被邀请人ID */
    private Long inviteeId;

    /** 邀请码 */
    private String inviteCode;

    /** 邀请奖励 */
    private String rewardAmount;

    /** 奖励币种 */
    private String rewardCoin;

    /** 奖励状态（0未发放 1已发放） */
    private Integer rewardStatus;

    /** 奖励发放时间 */
    private String rewardTime;

    /** 备注 */
    private String remark;
}
