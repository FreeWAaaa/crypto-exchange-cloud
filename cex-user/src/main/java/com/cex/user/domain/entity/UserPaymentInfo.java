package com.cex.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.cex.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户支付信息实体
 * 
 * @author cex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_payment_info")
public class UserPaymentInfo extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 支付类型（1银行卡 2支付宝 3微信） */
    private Integer paymentType;

    /** 银行名称 */
    private String bankName;

    /** 银行卡号 */
    private String bankCard;

    /** 开户行 */
    private String bankBranch;

    /** 支付宝账号 */
    private String alipayAccount;

    /** 微信账号 */
    private String wechatAccount;

    /** 收款人姓名 */
    private String realName;

    /** 收款二维码 */
    private String qrCode;

    /** 状态（0停用 1正常） */
    private Integer status;

    /** 备注 */
    private String remark;
}

