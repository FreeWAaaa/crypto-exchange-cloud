package com.cex.notification.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.cex.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知实体
 * 
 * @author cex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notification")
public class Notification extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 通知标题 */
    private String title;

    /** 通知内容 */
    private String content;

    /** 通知类型（1系统通知 2交易通知 3活动通知 4安全通知） */
    private Integer type;

    /** 通知状态（0未读 1已读） */
    private Integer status;

    /** 是否推送（0不推送 1推送） */
    private Integer isPush;

    /** 推送时间 */
    private String pushTime;

    /** 阅读时间 */
    private String readTime;

    /** 备注 */
    private String remark;
}
