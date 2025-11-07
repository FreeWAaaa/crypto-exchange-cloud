package com.cex.notification.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.cex.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 短信发送记录
 * 
 * @author cex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sms_record")
public class SmsRecord extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 手机号 */
    private String mobile;

    /** 短信内容 */
    private String content;

    /** 短信类型（1验证码 2通知 3营销） */
    private Integer smsType;

    /** 发送状态（0待发送 1已发送 2发送失败） */
    private Integer status;

    /** 第三方响应码 */
    private String responseCode;

    /** 第三方响应消息 */
    private String responseMessage;

    /** 发送时间 */
    private java.util.Date sendTime;

    /** 重试次数 */
    private Integer retryCount;
}

