package com.cex.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.cex.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实名认证实体
 * 
 * @author cex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_verification")
public class UserVerification extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 真实姓名 */
    private String realName;

    /** 身份证号 */
    private String idCard;

    /** 身份证正面照片 */
    private String idCardFront;

    /** 身份证反面照片 */
    private String idCardBack;

    /** 手持身份证照片 */
    private String idCardHand;

    /** 认证状态（0待审核 1审核通过 2审核拒绝） */
    private Integer status;

    /** 审核人ID */
    private Long auditorId;

    /** 审核时间 */
    private String auditTime;

    /** 审核备注 */
    private String auditRemark;

    /** 拒绝原因 */
    private String rejectReason;
}
