package com.cex.user.service;

import com.cex.user.domain.dto.UserInviteStatDTO;
import com.cex.user.domain.entity.User;
import com.cex.user.domain.entity.UserInviteRelation;

import java.util.List;

/**
 * 用户邀请服务接口
 * 
 * @author cex
 */
public interface UserInviteService {

    /**
     * 创建邀请关系（支持三级）
     * 
     * @param userId 新用户ID
     * @param inviterId 邀请人ID
     */
    void createInviteRelation(Long userId, Long inviterId);

    /**
     * 查询用户的邀请列表
     * 
     * @param userId 用户ID
     * @param level 层级（1/2/3，null表示全部）
     * @return 邀请的用户列表
     */
    List<User> getInviteList(Long userId, Integer level);

    /**
     * 获取用户邀请统计
     * 
     * @param userId 用户ID
     * @return 邀请统计信息
     */
    UserInviteStatDTO getInviteStatistics(Long userId);

    /**
     * 查询用户的邀请关系
     * 
     * @param userId 用户ID
     * @return 邀请关系列表
     */
    List<UserInviteRelation> getUserInviteRelations(Long userId);
}

