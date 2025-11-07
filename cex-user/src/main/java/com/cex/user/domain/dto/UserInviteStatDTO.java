package com.cex.user.domain.dto;

import lombok.Data;

/**
 * 用户邀请统计DTO
 * 
 * @author cex
 */
@Data
public class UserInviteStatDTO {

    /**
     * 邀请码
     */
    private String inviteCode;

    /**
     * 一级邀请人数
     */
    private Integer firstLevelCount;

    /**
     * 二级邀请人数
     */
    private Integer secondLevelCount;

    /**
     * 三级邀请人数
     */
    private Integer thirdLevelCount;

    /**
     * 总邀请人数
     */
    private Integer totalCount;

    /**
     * 推广链接
     */
    private String promoteUrl;

    /**
     * 计算总人数
     */
    public void calculateTotal() {
        this.totalCount = (firstLevelCount != null ? firstLevelCount : 0) +
                          (secondLevelCount != null ? secondLevelCount : 0) +
                          (thirdLevelCount != null ? thirdLevelCount : 0);
    }
}

