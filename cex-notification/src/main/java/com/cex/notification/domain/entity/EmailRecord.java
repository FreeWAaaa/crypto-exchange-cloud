package com.cex.notification.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.cex.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 邮件发送记录
 * 
 * @author cex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("email_record")
public class EmailRecord extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 收件人邮箱 */
    private String toEmail;

    /** 邮件主题 */
    private String subject;

    /** 邮件内容 */
    private String content;

    /** 邮件类型（1验证码 2通知 3营销） */
    private Integer emailType;

    /** 发送状态（0待发送 1已发送 2发送失败） */
    private Integer status;

    /** 错误信息 */
    private String errorMessage;

    /** 发送时间 */
    private java.util.Date sendTime;

    /** 重试次数 */
    private Integer retryCount;
}

