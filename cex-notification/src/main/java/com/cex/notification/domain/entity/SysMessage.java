package com.cex.notification.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.cex.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 站内消息实体
 * 
 * @author cex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_message")
public class SysMessage extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 用户ID（0表示系统消息） */
    private Long userId;

    /** 消息标题 */
    private String title;

    /** 消息内容 */
    private String content;

    /** 消息类型（1系统通知 2交易通知 3活动通知） */
    private Integer messageType;

    /** 是否已读（0未读 1已读） */
    private Integer isRead;

    /** 关联业务ID */
    private String businessId;

    /** 关联业务类型 */
    private String businessType;

    /** 链接地址 */
    private String link;
}

